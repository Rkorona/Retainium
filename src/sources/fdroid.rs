use super::{AppConfig, ReleaseInfo, UpdateSource};
use anyhow::{anyhow, Context, Result};
use async_trait::async_trait;
use serde::Deserialize;
use std::collections::HashMap;

/// F-Droid 的 index-v2 结构做了大幅精简，只解析我们需要的字段。
/// 完整格式见 https://f-droid.org/docs/Data_Format/
#[derive(Debug, Deserialize)]
struct FDroidIndex {
    packages: HashMap<String, FDroidPackage>,
}

#[derive(Debug, Deserialize)]
struct FDroidPackage {
    versions: HashMap<String, FDroidVersion>,
}

#[derive(Debug, Deserialize)]
struct FDroidVersion {
    manifest: FDroidManifest,
    file: FDroidFile,
}

#[derive(Debug, Deserialize)]
struct FDroidManifest {
    #[serde(rename = "versionName")]
    version_name: String,
    #[serde(rename = "versionCode")]
    version_code: i64,
}

#[derive(Debug, Deserialize)]
struct FDroidFile {
    name: String, // 形如 "/PackageName_123.apk"
}

pub struct FDroidSource {
    client: reqwest::Client,
}

impl FDroidSource {
    pub fn new() -> Self {
        Self {
            client: reqwest::Client::builder()
                .user_agent("retainium/0.1")
                .build()
                .expect("failed to build reqwest client"),
        }
    }
}

#[async_trait]
impl UpdateSource for FDroidSource {
    async fn fetch_latest(&self, config: &AppConfig) -> Result<ReleaseInfo> {
        // source_identifier 就是 F-Droid 的 package id，如 "org.fdroid.fdroid"
        // 注意：index-v2.json 是全量索引（几十 MB），生产环境应该做本地缓存 + ETag，
        // 这里先给出最简单能跑通的版本。
        let index_url = "https://f-droid.org/repo/index-v2.json";

        let index: FDroidIndex = self
            .client
            .get(index_url)
            .send()
            .await
            .context("下载 F-Droid index-v2.json 失败")?
            .error_for_status()
            .context("F-Droid 仓库返回错误状态")?
            .json()
            .await
            .context("解析 F-Droid index JSON 失败（索引结构可能已变化，需要更新解析逻辑）")?;

        let package = index
            .packages
            .get(&config.source_identifier)
            .ok_or_else(|| {
                anyhow!("F-Droid 索引中未找到 package: {}", config.source_identifier)
            })?;

        let latest = package
            .versions
            .values()
            .max_by_key(|v| v.manifest.version_code)
            .ok_or_else(|| anyhow!("package {} 没有任何版本", config.source_identifier))?;

        let apk_filename = latest
            .file
            .name
            .trim_start_matches('/')
            .to_string();

        Ok(ReleaseInfo {
            version_name: latest.manifest.version_name.clone(),
            version_code: Some(latest.manifest.version_code),
            apk_url: format!("https://f-droid.org/repo/{}", apk_filename),
            apk_filename,
            release_notes: None,
        })
    }
}
