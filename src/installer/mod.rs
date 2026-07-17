pub mod rish;
pub mod adb;

use anyhow::Result;
use async_trait::async_trait;
use std::path::Path;

/// 安装结果。区分"成功""需要用户交互确认""失败"三种情况，
/// 因为 rish/adb 的静默安装在权限不足时会退化成需要用户点确认。
#[derive(Debug)]
pub enum InstallOutcome {
    Success,
    RequiresUserConfirmation,
    Failed(String),
}

#[async_trait]
pub trait Installer: Send + Sync {
    /// 检查该安装方式当前是否可用（比如 rish 是否已连接 Shizuku，adb 是否已连接设备）。
    /// 在真正尝试安装前调用，避免安装到一半才发现环境没配好。
    async fn is_available(&self) -> bool;

    /// 静默安装本地 APK 文件。
    async fn install(&self, apk_path: &Path) -> Result<InstallOutcome>;
    
    async fn installed_version(&self, package_name: &str) -> Option<(String, Option<i64>)>;
}

#[derive(Debug, Clone, Copy, clap::ValueEnum)]
pub enum InstallerBackend {
    Rish,
    Adb,
}

pub fn installer_for(backend: InstallerBackend) -> Box<dyn Installer> {
    match backend {
        InstallerBackend::Rish => Box::new(rish::RishInstaller::new()),
        InstallerBackend::Adb => Box::new(adb::AdbInstaller::new()),
    }
}

/// 从 `pm dump <package>` 的输出里提取 versionName / versionCode。
/// 两者都在 "Packages:" 段落下，形如：
///     versionName=2.9.3
///     versionCode=20903 minSdk=... targetSdk=...
pub(crate) fn parse_pm_dump_version(output: &str) -> Option<(String, Option<i64>)> {
    let version_name = output
        .lines()
        .find_map(|l| l.trim().strip_prefix("versionName="))
        .map(|s| s.trim().to_string())?;

    let version_code = output
        .lines()
        .find_map(|l| l.trim().strip_prefix("versionCode="))
        .and_then(|s| s.split_whitespace().next())
        .and_then(|s| s.parse::<i64>().ok());

    Some((version_name, version_code))
}