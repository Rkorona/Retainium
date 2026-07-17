mod config;
mod installer;
mod sources;
mod storage;

use anyhow::{Context, Result};
use clap::{Parser, Subcommand};
use config::AppEntry;
use installer::{installer_for, InstallOutcome, Installer, InstallerBackend};
use sources::{source_for, AppConfig, SourceType, UpdateCheckResult};
use std::path::PathBuf;
use storage::Storage;

#[derive(Parser)]
#[command(
    name = "retainium",
    about = "Termux + Shizuku/adb 下的开源应用更新管理器"
)]
struct Cli {
    /// 订阅配置文件路径（TOML，可直接编辑），默认 ~/.config/retainium/apps.toml
    #[arg(long, global = true)]
    config: Option<PathBuf>,

    /// 安装历史数据库路径，默认 ~/.local/share/retainium/retainium.db
    #[arg(long, global = true)]
    db: Option<PathBuf>,

    /// 安装方式：rish（Shizuku）或 adb
    #[arg(long, global = true, value_enum, default_value = "rish")]
    installer: InstallerBackend,

    /// APK 下载/暂存目录。必须是 Shizuku/adb 能访问到的路径。
    /// 用 rish 时建议指向 /sdcard/Download/retainium。
    #[arg(long, global = true)]
    download_dir: Option<PathBuf>,

    #[command(subcommand)]
    command: Command,
}

#[derive(Subcommand)]
enum Command {
    /// 添加一个新的订阅（写入 TOML 配置文件）
    Add {
        /// 自定义 app 标识，如 "obtainium"
        id: String,
        /// 更新源类型
        #[arg(value_enum)]
        source: SourceType,
        /// 源标识：GitHub/GitLab 用 "owner/repo"，F-Droid 用 package id
        identifier: String,
        /// 当 release 有多个 APK 附件时，用于筛选文件名的关键字
        #[arg(long)]
        apk_pattern: Option<String>,
    },
    /// 移除一个订阅
    Remove { id: String },
    /// 列出所有订阅
    List,
    /// 打印配置文件路径（方便你直接用编辑器打开手动改 apk_pattern 之类的字段）
    ConfigPath,
    /// 检查所有订阅的更新（不安装）
    Check,
    /// 检查并安装所有有更新的订阅
    Update {
        /// 只更新指定 id，不传则更新全部
        id: Option<String>,
    },
    /// 查看某个 app 的安装历史
    History { id: String },
}

impl clap::ValueEnum for SourceType {
    fn value_variants<'a>() -> &'a [Self] {
        &[SourceType::GitHub, SourceType::GitLab, SourceType::FDroid]
    }

    fn to_possible_value(&self) -> Option<clap::builder::PossibleValue> {
        Some(match self {
            SourceType::GitHub => clap::builder::PossibleValue::new("github"),
            SourceType::GitLab => clap::builder::PossibleValue::new("gitlab"),
            SourceType::FDroid => clap::builder::PossibleValue::new("fdroid"),
        })
    }
}

#[tokio::main]
async fn main() -> Result<()> {
    tracing_subscriber::fmt::init();
    let cli = Cli::parse();

    let config_path = cli.config.unwrap_or_else(config::default_config_path);
    let db_path = cli.db.unwrap_or_else(default_db_path);
    if let Some(parent) = db_path.parent() {
        std::fs::create_dir_all(parent).ok();
    }
    let storage = Storage::open(&db_path)?;

    let download_dir = cli
        .download_dir
        .unwrap_or_else(|| default_download_dir(cli.installer));
    std::fs::create_dir_all(&download_dir)
        .with_context(|| format!("创建下载目录失败: {}", download_dir.display()))?;

    match cli.command {
        Command::Add {
            id,
            source,
            identifier,
            apk_pattern,
        } => {
            let mut file_config = config::load(&config_path)?;
            if file_config.app.iter().any(|a| a.id == id) {
                anyhow::bail!(
                    "订阅 {id} 已存在，先用 `retainium remove {id}` 删除或直接编辑配置文件: {}",
                    config_path.display()
                );
            }
            file_config.app.push(AppEntry {
                id: id.clone(),
                source,
                identifier,
                apk_pattern,
                apk_exclude_pattern: None,
                package_name: None,
            });
            config::save(&config_path, &file_config)?;
            println!("已添加订阅: {id}（配置文件: {}）", config_path.display());
        }

        Command::Remove { id } => {
            let mut file_config = config::load(&config_path)?;
            let before = file_config.app.len();
            file_config.app.retain(|a| a.id != id);
            if file_config.app.len() == before {
                println!("未找到订阅: {id}");
            } else {
                config::save(&config_path, &file_config)?;
                println!("已移除订阅: {id}");
            }
        }

        Command::List => {
            let file_config = config::load(&config_path)?;
            if file_config.app.is_empty() {
                println!(
                    "暂无订阅，用 `retainium add` 添加，或直接编辑 {}",
                    config_path.display()
                );
            }
            for entry in &file_config.app {
                let installed = storage
                    .latest_installed_version(&entry.id)?
                    .map(|r| r.version_name)
                    .unwrap_or_else(|| "(未安装)".to_string());
                println!(
                    "{}\t{:?}\t{}\t当前版本: {}",
                    entry.id, entry.source, entry.identifier, installed
                );
            }
        }

        Command::ConfigPath => {
            println!("{}", config_path.display());
        }

        Command::Check => {
            let file_config = config::load(&config_path)?;
            let backend = installer_for(cli.installer);
            for entry in &file_config.app {
                let app = build_app_config(entry, &storage, backend.as_ref()).await?;
                match check_one(&app).await {
                    Ok(UpdateCheckResult::UpToDate) => {
                        println!("{}: 已是最新", app.id);
                    }
                    Ok(UpdateCheckResult::UpdateAvailable(release)) => {
                        println!("{}: 有更新 -> {}", app.id, release.version_name);
                    }
                    Err(e) => {
                        eprintln!("{}: 检查失败 - {e:#}", app.id);
                    }
                }
            }
        }

        Command::Update { id } => {
            let file_config = config::load(&config_path)?;
            let backend = installer_for(cli.installer);

            if !backend.is_available().await {
                eprintln!(
                    "警告: 安装后端 {} 当前不可用，请检查 rish/adb 连接状态后重试",
                    match cli.installer {
                        InstallerBackend::Rish => "rish",
                        InstallerBackend::Adb => "adb",
                    }
                );
                return Ok(());
            }

            for entry in &file_config.app {
                if let Some(target_id) = &id {
                    if &entry.id != target_id {
                        continue;
                    }
                }

                let app = build_app_config(entry, &storage, backend.as_ref()).await?;

                match check_one(&app).await {
                    Ok(UpdateCheckResult::UpToDate) => {
                        println!("{}: 已是最新，跳过", app.id);
                    }
                    Ok(UpdateCheckResult::UpdateAvailable(release)) => {
                        println!(
                            "{}: 发现新版本 {}，开始下载...",
                            app.id, release.version_name
                        );

                        let apk_path = download_dir.join(&release.apk_filename);
                        if apk_path.exists() {
                            println!(
                                "{}: 检测到已存在的文件 {}，跳过下载",
                                app.id,
                                apk_path.display()
                            );
                        } else if let Err(e) = download_apk(&release.apk_url, &apk_path).await {
                            eprintln!("{}: 下载失败 - {e:#}", app.id);
                            continue;
                        }

                        // 尽量从 APK 提取包名：优先用已知的，否则用 aapt（需 `pkg install aapt`）
                        let pkg_name: Option<String> = app.package_name.clone()
                            .or_else(|| installer::extract_package_name(&apk_path));

                        println!("{}: 下载完成，开始安装...", app.id);
                        match backend.install(&apk_path).await {
                            Ok(InstallOutcome::Success) => {
                                println!("{}: 安装成功 ({})", app.id, release.version_name);
                                storage.record_install(
                                    &app.id,
                                    &release.version_name,
                                    release.version_code,
                                    &release.apk_url,
                                    true,
                                    pkg_name.as_deref(),
                                )?;
                            }
                            Ok(InstallOutcome::RequiresUserConfirmation) => {
                                println!(
                                    "{}: 需要手动确认安装（APK 已下载到 {}）",
                                    app.id,
                                    apk_path.display()
                                );
                                storage.record_install(
                                    &app.id,
                                    &release.version_name,
                                    release.version_code,
                                    &release.apk_url,
                                    false,
                                    pkg_name.as_deref(),
                                )?;
                            }
                            Ok(InstallOutcome::Failed(msg)) => {
                                eprintln!("{}: 安装失败 - {msg}", app.id);
                                storage.record_install(
                                    &app.id,
                                    &release.version_name,
                                    release.version_code,
                                    &release.apk_url,
                                    false,
                                    pkg_name.as_deref(),
                                )?;
                            }
                            Err(e) => {
                                eprintln!("{}: 安装出错 - {e:#}", app.id);
                            }
                        }
                    }
                    Err(e) => {
                        eprintln!("{}: 检查失败 - {e:#}", app.id);
                    }
                }
            }
        }

        Command::History { id } => {
            let records = storage.history_for(&id)?;
            if records.is_empty() {
                println!("{id}: 无安装历史");
            }
            for r in records {
                println!(
                    "[{}] {} - {} ({})",
                    r.installed_at.format("%Y-%m-%d %H:%M"),
                    r.version_name,
                    if r.success {
                        "成功"
                    } else {
                        "失败/待确认"
                    },
                    r.apk_url
                );
            }
        }
    }

    Ok(())
}

/// 把 TOML 里的静态配置 + SQLite 里的运行时状态（已装版本）合并成
/// UpdateSource 需要的完整 AppConfig。
///
/// 包名解析优先级（从高到低）：
///   1. TOML 中显式配置的 package_name
///   2. F-Droid 订阅：source_identifier 本身就是包名
///   3. 数据库里上次安装时记录的包名
/// 只要拿到包名，就直接查询设备真实已安装版本（pm dump），
/// 而不依赖可能被删除的 SQLite 历史。
async fn build_app_config(
    entry: &AppEntry,
    storage: &Storage,
    backend: &dyn Installer,
) -> Result<AppConfig> {
    let mut app: AppConfig = entry.into();

    // 三级级联解析包名
    let pkg: Option<String> = if let Some(p) = &entry.package_name {
        Some(p.clone())
    } else if entry.source == sources::SourceType::FDroid {
        // F-Droid 的 identifier 就是 Android 包名
        Some(entry.identifier.clone())
    } else {
        // 从历史记录里取上次安装时保存的包名
        storage.stored_package_name(&entry.id)?
    };

    // 有包名就查设备真实版本，优先于数据库历史
    if let Some(ref p) = pkg {
        if let Some((version_name, _version_code)) = backend.installed_version(p).await {
            app.installed_version = Some(version_name);
            app.package_name = Some(p.clone());
            return Ok(app);
        }
    }

    // 回退到数据库历史（设备未安装或包名未知）
    app.installed_version = storage
        .latest_installed_version(&entry.id)?
        .map(|r| r.version_name);
    if let Some(p) = pkg {
        app.package_name = Some(p);
    }
    Ok(app)
}

async fn check_one(app: &AppConfig) -> Result<UpdateCheckResult> {
    let source = source_for(app.source_type);
    let latest = source.fetch_latest(app).await?;
    Ok(sources::compare_versions(
        app.installed_version.as_deref(),
        None, // version_code 暂未持久化比较，见 storage::InstallRecord.version_code 可扩展
        &latest,
    ))
}

async fn download_apk(url: &str, dest: &std::path::Path) -> Result<()> {
    let client = reqwest::Client::builder()
        .timeout(std::time::Duration::from_secs(60))
        .connect_timeout(std::time::Duration::from_secs(15))
        .build()
        .context("构建 HTTP 客户端失败")?;

    let response = client
        .get(url)
        .send()
        .await
        .with_context(|| {
            format!(
                "下载失败: {url}\n\
             （15秒内无法建立连接，通常是代理没生效或网络不通。\
             可以先用浏览器手动下载到 {} 目录，工具会跳过已存在的同名文件重新下载）",
                dest.parent()
                    .map(|p| p.display().to_string())
                    .unwrap_or_default()
            )
        })?
        .error_for_status()
        .with_context(|| format!("下载返回错误状态: {url}"))?;

    let bytes = response.bytes().await.context("读取响应体失败")?;
    tokio::fs::write(dest, &bytes)
        .await
        .with_context(|| format!("写入文件失败: {}", dest.display()))?;

    Ok(())
}

fn default_db_path() -> PathBuf {
    let home = std::env::var("HOME").unwrap_or_else(|_| ".".to_string());
    PathBuf::from(home).join(".local/share/retainium/retainium.db")
}

fn default_download_dir(backend: InstallerBackend) -> PathBuf {
    match backend {
        InstallerBackend::Rish => PathBuf::from("/sdcard/Download/retainium"),
        InstallerBackend::Adb => {
            let home = std::env::var("HOME").unwrap_or_else(|_| ".".to_string());
            PathBuf::from(home).join(".cache/retainium/apks")
        }
    }
}
