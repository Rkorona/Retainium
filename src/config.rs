use crate::sources::{AppConfig, SourceType};
use anyhow::{Context, Result};
use serde::{Deserialize, Serialize};
use std::path::{Path, PathBuf};

/// 磁盘上的配置文件结构。
///
/// 特意跟 sources::AppConfig 分开定义（即使字段几乎一样），因为：
/// 1. AppConfig 里的 installed_version 是运行时状态，直接查询设备获取，不由用户在配置文件里维护。
/// 2. 配置文件的字段命名要顾及可读性（用户会直接编辑），不用完全照搬内部类型。
#[derive(Debug, Serialize, Deserialize)]
pub struct FileConfig {
    #[serde(default)]
    pub app: Vec<AppEntry>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AppEntry {
    pub id: String,
    pub source: SourceType,
    pub identifier: String,
    /// 当 release 有多个 APK 附件时，用于筛选文件名的关键字（子串匹配）。
    /// 例如 obtainium 的 release 里有 8 个 APK，按架构和 fdroid/普通版分包，
    /// 想要 64 位普通版就设 "arm64-v8a-release"（注意不要匹配到 "fdroid-release"）。
    #[serde(default)]
    pub apk_pattern: Option<String>,
    #[serde(default)]
    pub apk_exclude_pattern: Option<String>,
    
    /// 用于安装后校验包名是否正确（可选，暂未在安装流程中启用校验逻辑）
    #[serde(default)]
    pub package_name: Option<String>,
}

impl From<&AppEntry> for AppConfig {

    fn from(entry: &AppEntry) -> Self {
        AppConfig {
            id: entry.id.clone(),
            source_type: entry.source,
            source_identifier: entry.identifier.clone(),
            apk_pattern: entry.apk_pattern.clone(),
            apk_exclude_pattern: entry.apk_exclude_pattern.clone(),
            installed_version: None, // 由调用方通过 pm dump 查询设备后填充
            package_name: entry.package_name.clone(),
        }
    }
}

pub fn load(path: &Path) -> Result<FileConfig> {
    if !path.exists() {
        return Ok(FileConfig { app: Vec::new() });
    }
    let text = std::fs::read_to_string(path)
        .with_context(|| format!("读取配置文件失败: {}", path.display()))?;
    toml::from_str(&text)
        .with_context(|| format!("解析配置文件失败（TOML 格式错误）: {}", path.display()))
}

pub fn save(path: &Path, config: &FileConfig) -> Result<()> {
    if let Some(parent) = path.parent() {
        std::fs::create_dir_all(parent).ok();
    }
    let text = toml::to_string_pretty(config).context("序列化配置为 TOML 失败")?;
    std::fs::write(path, text).with_context(|| format!("写入配置文件失败: {}", path.display()))?;
    Ok(())
}

pub fn default_config_path() -> PathBuf {
    let home = std::env::var("HOME").unwrap_or_else(|_| ".".to_string());
    PathBuf::from(home).join(".config/retainium/apps.toml")
}
