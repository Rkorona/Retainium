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

        // 第一步：硬排除（apk_exclude_pattern 始终生效）
        let after_exclude: Vec<&GlLink> = release
            .assets
            .links
            .iter()
            .filter(|l| l.name.ends_with(".apk") || l.url.ends_with(".apk"))
            .filter(|l| {
                config
                    .apk_exclude_pattern
                    .as_ref()
                    .map(|pat| !l.name.contains(pat.as_str()))
                    .unwrap_or(true)
            })
            .collect();

        // 第二步：在排除后的候选里再用 apk_pattern 精选
        let after_pattern: Vec<&GlLink> = after_exclude
            .iter()
            .copied()
            .filter(|l| {
                config
                    .apk_pattern
                    .as_ref()
                    .map(|pat| l.name.contains(pat.as_str()))
                    .unwrap_or(true)
            })
            .collect();

        let link = match (after_pattern.len(), after_exclude.len()) {
            (1, _) => after_pattern[0],
            (n, _) if n > 1 => return Err(anyhow!(
                "release {} 中有 {} 个匹配的 APK，请设置更精确的 apk_pattern 来筛选: {:?}",
                release.tag_name,
                n,
                after_pattern.iter().map(|l| &l.name).collect::<Vec<_>>()
            )),
            (0, 1) => after_exclude[0], // pattern 未命中，但唯一候选无歧义
            _ => return Err(anyhow!(
                "release {} 中没有匹配的 APK 附件（pattern: {:?}，排除后剩余: {:?}）",
                release.tag_name,
                config.apk_pattern,
                after_exclude.iter().map(|l| &l.name).collect::<Vec<_>>()
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
