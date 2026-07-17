use super::{InstallOutcome, Installer};
use anyhow::{Context, Result};
use async_trait::async_trait;
use std::path::Path;
use tokio::process::Command;

/// 通过 rish（Shizuku 官方提供的 shell 桥接脚本）执行 `pm install`。
///
/// 两个关键坑，踩过才知道，记录下来避免以后回归：
///
/// 1. **PATH 问题**：rish 转发命令运行的 shell 环境 PATH 里不含 `pm` 所在目录，
///    必须用绝对路径 `/system/bin/pm`，否则报 "pm: No such file or directory"。
///
/// 2. **SELinux 读权限问题**：`pm install` 内部由 system_server 进程执行实际的文件读取，
///    而 system_server 的 SELinux 域对 `/sdcard/...`（FUSE 挂载）没有读权限
///    （报错: "System server has no access to read file context u:object_r:fuse:s0"）。
///    即使 rish/shell 用户自己能读 /sdcard，pm install 内部转发给 system_server 处理时依然会失败。
///    解法：先用 rish 把 APK 从 /sdcard 复制到 /data/local/tmp（shell 用户和 system_server
///    都有权限访问这个目录），再对这个路径执行 pm install。
pub struct RishInstaller {
    rish_path: String,
}

const PM_PATH: &str = "/system/bin/pm";
const STAGING_DIR: &str = "/data/local/tmp/retainium";

/// 用单引号包裹参数，防止路径中包含空格或 shell 特殊字符时，
/// 拼接成的 `-c` 命令字符串被错误解析。
/// 单引号内部只需处理单引号本身：'x' -> 'x'\''x'
fn shell_quote(s: &str) -> String {
    format!("'{}'", s.replace('\'', r#"'\''"#))
}

impl RishInstaller {
    pub fn new() -> Self {
        Self {
            rish_path: std::env::var("RETAINIUM_RISH_PATH")
                .unwrap_or_else(|_| "rish".to_string()),
        }
    }

    /// 关键点：rish 要求把待执行命令作为一整条字符串传给 `-c`，
    /// 例如 `rish -c 'mkdir -p /data/local/tmp/retainium'`。
    /// 如果像 `rish mkdir -p ...` 这样把参数分开传，rish 不会按预期转发执行，
    /// 但自身进程仍会正常退出（exit=0、stdout/stderr 为空），造成"假成功"的假象——
    /// 这正是本文件之前踩过的坑：mkdir/cp 的 exit=0 并不代表命令真的被执行了。
    async fn run_rish(&self, args: &[&str]) -> Result<std::process::Output> {
        let cmd_str = args
            .iter()
            .map(|a| shell_quote(a))
            .collect::<Vec<_>>()
            .join(" ");
        Command::new(&self.rish_path)
            .arg("-c")
            .arg(&cmd_str)
            .output()
            .await
            .with_context(|| format!("执行 rish 失败（路径: {}，命令: {cmd_str}），请确认已设置 RETAINIUM_RISH_PATH 或 rish 在 PATH 中", self.rish_path))
    }
}

#[async_trait]
impl Installer for RishInstaller {
    async fn is_available(&self) -> bool {
        match self.run_rish(&["id"]).await {
            Ok(output) => output.status.success(),
            Err(_) => false,
        }
    }

    async fn install(&self, apk_path: &Path) -> Result<InstallOutcome> {
        let apk_str = apk_path
            .to_str()
            .context("APK 路径包含非法 UTF-8 字符")?;

        let filename = apk_path
            .file_name()
            .and_then(|n| n.to_str())
            .context("无法从 APK 路径提取文件名")?;
        let staged_path = format!("{STAGING_DIR}/{filename}");

        // 第一步：确保 staging 目录存在（mkdir -p 幂等，已存在不会报错）
        let mkdir_output = self.run_rish(&["mkdir", "-p", STAGING_DIR]).await?;
        if !mkdir_output.status.success() {
            return Ok(InstallOutcome::Failed(format!(
                "创建 staging 目录失败: {}",
                String::from_utf8_lossy(&mkdir_output.stderr)
            )));
        }

        // 第二步：把 APK 从 Termux 可写的共享存储路径复制到 system_server 可读的 /data/local/tmp。
        // 这一步是必须的，直接对 /sdcard 下的文件跑 pm install 会因为 SELinux 权限失败。
        let cp_output = self.run_rish(&["cp", apk_str, &staged_path]).await?;
        if !cp_output.status.success() {
            return Ok(InstallOutcome::Failed(format!(
                "复制 APK 到 {staged_path} 失败: {}",
                String::from_utf8_lossy(&cp_output.stderr)
            )));
        }

        // 第三步：对 staging 路径执行安装。-r 允许覆盖安装已存在的同包名应用。
        let install_output = self
            .run_rish(&[PM_PATH, "install", "-r", "--user", "0", &staged_path])
            .await?;

        let stdout = String::from_utf8_lossy(&install_output.stdout);
        let stderr = String::from_utf8_lossy(&install_output.stderr);

        // 无论成败都清理 staging 文件，避免 /data/local/tmp 堆积旧 APK
        let _ = self.run_rish(&["rm", "-f", &staged_path]).await;

        // 注意：经过 rish 转发后，stdout/stderr 经常是空的（不代表命令没执行），
        // 所以不能要求 stdout 必须包含 "Success" 字样才算成功，应优先信任 exit code。
        if install_output.status.success()
            && !stderr.contains("Failure")
            && !stdout.contains("Failure")
        {
            Ok(InstallOutcome::Success)
        } else if stderr.contains("INSTALL_FAILED_USER_RESTRICTED")
            || stderr.contains("permission")
        {
            Ok(InstallOutcome::RequiresUserConfirmation)
        } else {
            Ok(InstallOutcome::Failed(format!(
                "stdout: {stdout}\nstderr: {stderr}"
            )))
        }
    }
    async fn installed_version(&self, package_name: &str) -> Option<(String, Option<i64>)> {
        // pm dump は読み取り専用で Shizuku 権限不要。
        // rish 経由だと stdout がパイプに届かず空になる既知問題があるため、
        // Termux から pm を直接呼び出して stdout を正しく取得する。
        // pm dump 是只读查询，不需要 Shizuku 权限。
        // 经 rish 转发时 stdout 经常为空（已知问题），因此直接在 Termux 里调用 pm，
        // 可以正常捕获输出。
        let output = Command::new("pm")
            .args(["dump", package_name])
            .output()
            .await
            .ok()?;
        if !output.status.success() {
            return None;
        }
        let stdout = String::from_utf8_lossy(&output.stdout);
        super::parse_pm_dump_version(&stdout)
    }
}

