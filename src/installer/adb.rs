use super::{InstallOutcome, Installer};
use anyhow::{Context, Result};
use async_trait::async_trait;
use std::path::Path;
use tokio::process::Command;

/// 通过 adb install 安装。适用于：
/// 1. 已用 `adb pair` / `adb connect` 连接到本机（Android 11+ 无线调试）；
/// 2. 或者手机本身跑了 adb server 允许回环连接。
///
/// 相比 rish 的优势：`adb install` 自己处理文件推送，不用操心 Shizuku 进程
/// 能不能访问 Termux 目录 —— adb 会先 push 到设备再触发安装。
pub struct AdbInstaller {
    adb_path: String,
}

impl AdbInstaller {
    pub fn new() -> Self {
        Self {
            adb_path: std::env::var("RETAINIUM_ADB_PATH")
                .unwrap_or_else(|_| "adb".to_string()),
        }
    }
}

#[async_trait]
impl Installer for AdbInstaller {
    async fn is_available(&self) -> bool {
        match Command::new(&self.adb_path).arg("devices").output().await {
            Ok(output) => {
                let stdout = String::from_utf8_lossy(&output.stdout);
                // "devices" 的输出第一行是标题，之后每行一个已连接设备，
                // 至少要有一行且状态是 "device"（不是 "unauthorized"/"offline"）
                stdout
                    .lines()
                    .skip(1)
                    .any(|l| l.contains('\t') && l.ends_with("device"))
            }
            Err(_) => false,
        }
    }

    async fn install(&self, apk_path: &Path) -> Result<InstallOutcome> {
        let apk_str = apk_path
            .to_str()
            .context("APK 路径包含非法 UTF-8 字符")?;

        // -r: 覆盖安装已有同包名应用
        let output = Command::new(&self.adb_path)
            .args(["install", "-r", apk_str])
            .output()
            .await
            .context("执行 adb install 失败，请确认 adb 已安装且设备已连接（adb devices 检查）")?;

        let stdout = String::from_utf8_lossy(&output.stdout);
        let stderr = String::from_utf8_lossy(&output.stderr);

        if output.status.success() && stdout.contains("Success") {
            Ok(InstallOutcome::Success)
        } else {
            Ok(InstallOutcome::Failed(format!(
                "stdout: {stdout}\nstderr: {stderr}"
            )))
        }
    }
    
    async fn installed_version(&self, package_name: &str) -> Option<(String, Option<i64>)> {
        let output = Command::new(&self.adb_path)
            .args(["shell", "pm", "dump", package_name])
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
