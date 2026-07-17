use super::{AppConfig, ReleaseInfo, UpdateSource};
use anyhow::{anyhow, Context, Result};
use async_trait::async_trait;
use serde::Deserialize;

const USER_AGENT: &str = "retainium/0.1";
/// GitHub API 未认证请求限额只有 60 次/小时，很容易触发 403 rate limit exceeded。
/// 设置这个环境变量为一个 GitHub Personal Access Token（无需任何权限范围，
/// 只读 public repo 用最基础的 classic token 即可）后，限额可提升到 5000 次/小时。
const GITHUB_TOKEN_ENV: &str = "RETAINIUM_GITHUB_TOKEN";

#[derive(Debug, Deserialize)]
struct GhRelease {
    tag_name: String,
    body: Option<String>,
    assets: Vec<GhAsset>,
    prerelease: bool,
    draft: bool,
}

#[derive(Debug, Deserialize)]
struct GhAsset {
    name: String,
    browser_download_url: String,
}

pub struct GitHubSource {
    client: reqwest::Client,
    token: Option<String>,
}

impl GitHubSource {
    pub fn new() -> Self {
        Self {
            client: reqwest::Client::builder()
                .user_agent(USER_AGENT)
                .build()
                .expect("failed to build reqwest client"),
            token: std::env::var(GITHUB_TOKEN_ENV)
                .ok()
                .filter(|s| !s.is_empty()),
        }
    }
}

#[async_trait]
impl UpdateSource for GitHubSource {
    async fn fetch_latest(&self, config: &AppConfig) -> Result<ReleaseInfo> {
        // source_identifier 形如 "owner/repo"
        let url = format!(
            "https://api.github.com/repos/{}/releases",
            config.source_identifier
        );

        let mut request = self.client.get(&url);
        if let Some(token) = &self.token {
            request = request.header("Authorization", format!("Bearer {token}"));
        }

        let releases: Vec<GhRelease> = request
            .send()
            .await
            .context("请求 GitHub releases API 失败")?
            .error_for_status()
            .context("GitHub API 返回错误状态（检查仓库名是否正确，或是否触发速率限制；可设置 RETAINIUM_GITHUB_TOKEN 环境变量提升限额）")?
            .json()
            .await
            .context("解析 GitHub releases JSON 失败")?;

        let release = releases
            .into_iter()
            .find(|r| !r.draft && !r.prerelease)
            .ok_or_else(|| anyhow!("未找到正式发布版本（可能只有 draft/prerelease）"))?;

        // 第一步：硬排除（apk_exclude_pattern 始终生效）
        let after_exclude: Vec<&GhAsset> = release
            .assets
            .iter()
            .filter(|a| a.name.ends_with(".apk"))
            .filter(|a| {
                config
                    .apk_exclude_pattern
                    .as_ref()
                    .map(|pat| !a.name.contains(pat.as_str()))
                    .unwrap_or(true)
            })
            .collect();

        // 第二步：在排除后的候选里再用 apk_pattern 精选
        let after_pattern: Vec<&GhAsset> = after_exclude
            .iter()
            .copied()
            .filter(|a| {
                config
                    .apk_pattern
                    .as_ref()
                    .map(|pat| a.name.contains(pat.as_str()))
                    .unwrap_or(true)
            })
            .collect();

        // 选取逻辑：
        //   pattern 匹配到 1 个 → 直接用
        //   pattern 匹配到多个 → 报错（需要更精确的 pattern）
        //   pattern 匹配到 0 个，但排除后只剩 1 个 → 退化使用（开发者改了命名，pattern 失效但无歧义）
        //   其余情况 → 报错
        let asset = match (after_pattern.len(), after_exclude.len()) {
            (1, _) => after_pattern[0],
            (n, _) if n > 1 => {
                return Err(anyhow!(
                    "release {} 中有 {} 个匹配的 APK，请设置更精确的 apk_pattern 来筛选: {:?}",
                    release.tag_name,
                    n,
                    after_pattern.iter().map(|a| &a.name).collect::<Vec<_>>()
                ))
            }
            (0, 1) => after_exclude[0], // pattern 未命中，但唯一候选无歧义
            _ => {
                return Err(anyhow!(
                    "release {} 中没有匹配的 APK 附件（pattern: {:?}，排除后剩余: {:?}）",
                    release.tag_name,
                    config.apk_pattern,
                    after_exclude.iter().map(|a| &a.name).collect::<Vec<_>>()
                ))
            }
        };

        Ok(ReleaseInfo {
            version_name: release.tag_name,
            version_code: None, // GitHub releases 没有原生的 versionCode 概念
            apk_url: asset.browser_download_url.clone(),
            apk_filename: asset.name.clone(),
            release_notes: release.body,
        })
    }
}
