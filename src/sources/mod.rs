pub mod fdroid;
pub mod github;
pub mod gitlab;

use anyhow::Result;
use async_trait::async_trait;
use serde::{Deserialize, Serialize};

/// 一个可安装的更新记录：版本号 + APK 直链
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ReleaseInfo {
    /// 展示用版本名，如 "v2.3.1"
    pub version_name: String,
    /// 用于比较的数字版本号（如果源提供的话，否则用 semver 解析 version_name）
    pub version_code: Option<i64>,
    /// APK 直链
    pub apk_url: String,
    /// APK 文件名（用于本地缓存/展示）
    pub apk_filename: String,
    /// 发布说明（可选，截断展示）
    pub release_notes: Option<String>,
}

/// 一个"订阅"的应用来源配置。反序列化后传给对应的 Source 实现。
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AppConfig {
    pub id: String, // 用户自定义 app 标识，如 "obtainium"
    pub source_type: SourceType,
    pub source_identifier: String, // 例如 "owner/repo" (GitHub/GitLab) 或 F-Droid 的 package id
    pub apk_pattern: Option<String>, // 当 release 有多个 APK 附件时，用正则/关键字筛选
    pub apk_exclude_pattern: Option<String>,
    pub installed_version: Option<String>,
    pub installed_version_code: Option<i64>, // 来自 pm dump，用于 versionCode 数字比较
    pub package_name: Option<String>,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "lowercase")]
pub enum SourceType {
    GitHub,
    GitLab,
    FDroid,
}

/// 所有更新源的统一接口。
///
/// 实现者只需要知道"怎么从 source_identifier 拿到最新 release"，
/// 不需要关心下载、安装、版本比较之外的存储逻辑。
#[async_trait]
pub trait UpdateSource: Send + Sync {
    /// 拉取该 app 的最新一个发布版本信息。
    /// 如果源里有多个 APK 变体（比如按 ABI 分包），用 AppConfig.apk_pattern 过滤，
    /// 若过滤后仍有多个/零个匹配，应返回 Err 而不是猜测。
    async fn fetch_latest(&self, config: &AppConfig) -> Result<ReleaseInfo>;
}

/// 根据 SourceType 分发到具体实现。
pub fn source_for(source_type: SourceType) -> Box<dyn UpdateSource> {
    match source_type {
        SourceType::GitHub => Box::new(github::GitHubSource::new()),
        SourceType::GitLab => Box::new(gitlab::GitLabSource::new()),
        SourceType::FDroid => Box::new(fdroid::FDroidSource::new()),
    }
}

/// 版本比较结果
#[derive(Debug)]
pub enum UpdateCheckResult {
    UpToDate,
    UpdateAvailable(ReleaseInfo),
}

/// 统一的版本比较逻辑，优先级从高到低：
///   1. 双方都有 versionCode → 纯数字比较，最可靠
///   2. 严格 semver 解析（strip 'v' 前缀后直接 parse）
///   3. 宽松 semver：取版本字符串里前三个纯数字段组成 major.minor.patch 再比
///      （用于 "1.4.8.r349" vs "1.4.8" 这类 tag 与 versionName 不完全一致的情况）
///   4. 字符串不等兜底
pub fn compare_versions(
    installed: Option<&str>,
    installed_code: Option<i64>,
    latest: &ReleaseInfo,
) -> UpdateCheckResult {
    // 1. versionCode 数字比较
    if let (Some(inst_code), Some(latest_code)) = (installed_code, latest.version_code) {
        return if latest_code > inst_code {
            UpdateCheckResult::UpdateAvailable(latest.clone())
        } else {
            UpdateCheckResult::UpToDate
        };
    }

    let Some(installed) = installed else {
        return UpdateCheckResult::UpdateAvailable(latest.clone());
    };

    let inst_stripped = installed.trim_start_matches('v');
    let latest_stripped = latest.version_name.trim_start_matches('v');

    // 2. 严格 semver
    if let (Ok(iv), Ok(lv)) = (
        semver::Version::parse(inst_stripped),
        semver::Version::parse(latest_stripped),
    ) {
        return if lv > iv {
            UpdateCheckResult::UpdateAvailable(latest.clone())
        } else {
            UpdateCheckResult::UpToDate
        };
    }

    // 3. 宽松 semver：从点分字段里提取前三个纯数字段
    if let (Some(iv), Some(lv)) = (lenient_semver(inst_stripped), lenient_semver(latest_stripped)) {
        return if lv > iv {
            UpdateCheckResult::UpdateAvailable(latest.clone())
        } else {
            UpdateCheckResult::UpToDate
        };
    }

    // 4. 字符串兜底
    if inst_stripped != latest_stripped {
        UpdateCheckResult::UpdateAvailable(latest.clone())
    } else {
        UpdateCheckResult::UpToDate
    }
}

/// 宽松 semver 解析：从任意版本字符串里提取开头的纯数字点分段。
/// "1.4.8.r349" → 1.4.8，"2.0" → 2.0.0，"abc" → None
fn lenient_semver(s: &str) -> Option<semver::Version> {
    let parts: Vec<u64> = s
        .split('.')
        .take(3)
        .map(|seg| {
            // 只取每段开头连续的数字字符
            let digits: String = seg.chars().take_while(|c| c.is_ascii_digit()).collect();
            digits.parse::<u64>().ok()
        })
        .take_while(|v| v.is_some())
        .flatten()
        .collect();

    if parts.is_empty() {
        return None;
    }
    Some(semver::Version::new(
        parts[0],
        parts.get(1).copied().unwrap_or(0),
        parts.get(2).copied().unwrap_or(0),
    ))
}
