use super::{AppConfig, ReleaseInfo, UpdateSource};
use anyhow::{anyhow, Context, Result};
use async_trait::async_trait;
use serde::Deserialize;

#[derive(Debug, Deserialize)]
struct GlRelease {
    tag_name: String,
    description: Option<String>,
    assets: GlAssets,
}

#[derive(Debug, Deserialize)]
struct GlAssets {
    links: Vec<GlLink>,
}

#[derive(Debug, Deserialize)]
struct GlLink {
    name: String,
    url: String,
}

pub struct GitLabSource {
    client: reqwest::Client,
}

impl GitLabSource {
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
impl UpdateSource for GitLabSource {
    async fn fetch_latest(&self, config: &AppConfig) -> Result<ReleaseInfo> {
        // source_identifier 形如 "owner/repo"，GitLab API 需要 URL-encode 的 project path
        let project_encoded = urlencoding_light(&config.source_identifier);
        let url = format!(
            "https://gitlab.com/api/v4/projects/{}/releases",
            project_encoded
        );

        let releases: Vec<GlRelease> = self
            .client
            .get(&url)
            .send()
            .await
            .context("请求 GitLab releases API 失败")?
            .error_for_status()
            .context("GitLab API 返回错误状态（检查 project path 是否正确）")?
            .json()
            .await
            .context("解析 GitLab releases JSON 失败")?;

        let release = releases
            .into_iter()
            .next()
            .ok_or_else(|| anyhow!("该项目没有任何 release"))?;

        let apk_links: Vec<&GlLink> = release
            .assets
            .links
            .iter()
            .filter(|l| l.name.ends_with(".apk") || l.url.ends_with(".apk"))
            .filter(|l| {
                config
                    .apk_pattern
                    .as_ref()
                    .map(|pat| l.name.contains(pat.as_str()))
                    .unwrap_or(true)
            })
            .filter(|a: &&GlLink| {
                config.apk_exclude_pattern
                    .as_ref()
                    .map(|pat| !a.name.contains(pat.as_str()))
                    .unwrap_or(true)
            })
            .collect();

        let link = match apk_links.len() {
            0 => return Err(anyhow!(
                "release {} 中没有匹配的 APK 附件（pattern: {:?}）",
                release.tag_name, config.apk_pattern
            )),
            1 => apk_links[0],
            _ => return Err(anyhow!(
                "release {} 中有 {} 个匹配的 APK，请设置更精确的 apk_pattern",
                release.tag_name,
                apk_links.len()
            )),
        };

        Ok(ReleaseInfo {
            version_name: release.tag_name,
            version_code: None,
            apk_url: link.url.clone(),
            apk_filename: link
                .url
                .rsplit('/')
                .next()
                .unwrap_or(&link.name)
                .to_string(),
            release_notes: release.description,
        })
    }
}

/// 极简 path 编码：GitLab project path 只需要把 "/" 换成 "%2F"，
/// 避免引入额外的 urlencoding crate 依赖。
fn urlencoding_light(s: &str) -> String {
    s.replace('/', "%2F")
}
