/// Retainium/src/storage/mod.rs
use anyhow::{Context, Result};
use chrono::{DateTime, Utc};
use rusqlite::Connection;
use serde::{Deserialize, Serialize};
use std::path::Path;

/// 一条安装历史记录，用于后续实现"回滚"功能：
/// 保留每次成功安装的 APK 版本信息，回滚时可以重新下载/复用缓存的旧 APK 重装。
///
/// 注意：订阅列表（app id/source/identifier/apk_pattern）不再存这里，
/// 那些是用户手动维护的配置，见 config.rs（TOML 文件）。
/// SQLite 只存"程序运行产生的状态"：装过什么版本、什么时候装的、成不成功。
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct InstallRecord {
    pub id: i64,
    pub app_id: String,
    pub version_name: String,
    pub version_code: Option<i64>,
    pub apk_url: String,
    pub installed_at: DateTime<Utc>,
    pub success: bool,
}

pub struct Storage {
    conn: Connection,
}

impl Storage {
    pub fn open(db_path: &Path) -> Result<Self> {
        let conn = Connection::open(db_path)
            .with_context(|| format!("打开数据库失败: {}", db_path.display()))?;
        let storage = Self { conn };
        storage.migrate()?;
        Ok(storage)
    }

    fn migrate(&self) -> Result<()> {
        self.conn.execute_batch(
            r#"
            CREATE TABLE IF NOT EXISTS install_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                app_id TEXT NOT NULL,
                version_name TEXT NOT NULL,
                version_code INTEGER,
                apk_url TEXT NOT NULL,
                installed_at TEXT NOT NULL,
                success INTEGER NOT NULL
            );
            "#,
        )?;
        Ok(())
    }

    pub fn record_install(
        &self,
        app_id: &str,
        version_name: &str,
        version_code: Option<i64>,
        apk_url: &str,
        success: bool,
    ) -> Result<()> {
        self.conn.execute(
            "INSERT INTO install_history (app_id, version_name, version_code, apk_url, installed_at, success)
             VALUES (?1, ?2, ?3, ?4, ?5, ?6)",
            rusqlite::params![
                app_id,
                version_name,
                version_code,
                apk_url,
                Utc::now().to_rfc3339(),
                success as i32,
            ],
        )?;
        Ok(())
    }

    /// 取某个 app 的安装历史，最新的在前，用于回滚功能选择目标版本，
    /// 也用于展示 `history` 命令的输出。
    pub fn history_for(&self, app_id: &str) -> Result<Vec<InstallRecord>> {
        let mut stmt = self.conn.prepare(
            "SELECT id, app_id, version_name, version_code, apk_url, installed_at, success
             FROM install_history WHERE app_id = ?1 ORDER BY id DESC",
        )?;
        let rows = stmt.query_map([app_id], |row| {
            let installed_at_str: String = row.get(5)?;
            Ok(InstallRecord {
                id: row.get(0)?,
                app_id: row.get(1)?,
                version_name: row.get(2)?,
                version_code: row.get(3)?,
                apk_url: row.get(4)?,
                installed_at: DateTime::parse_from_rfc3339(&installed_at_str)
                    .map(|dt| dt.with_timezone(&Utc))
                    .unwrap_or_else(|_| Utc::now()),
                success: row.get::<_, i32>(6)? != 0,
            })
        })?;

        rows.collect::<rusqlite::Result<Vec<_>>>()
            .context("查询 install_history 表失败")
    }

    /// 取某个 app 最近一次"成功"安装的版本，作为版本比较的基准。
    /// 找不到就返回 None（视为从未安装过）。
    pub fn latest_installed_version(&self, app_id: &str) -> Result<Option<InstallRecord>> {
        let mut stmt = self.conn.prepare(
            "SELECT id, app_id, version_name, version_code, apk_url, installed_at, success
             FROM install_history WHERE app_id = ?1 AND success = 1 ORDER BY id DESC LIMIT 1",
        )?;
        let mut rows = stmt.query_map([app_id], |row| {
            let installed_at_str: String = row.get(5)?;
            Ok(InstallRecord {
                id: row.get(0)?,
                app_id: row.get(1)?,
                version_name: row.get(2)?,
                version_code: row.get(3)?,
                apk_url: row.get(4)?,
                installed_at: DateTime::parse_from_rfc3339(&installed_at_str)
                    .map(|dt| dt.with_timezone(&Utc))
                    .unwrap_or_else(|_| Utc::now()),
                success: row.get::<_, i32>(6)? != 0,
            })
        })?;

        match rows.next() {
            Some(r) => Ok(Some(r?)),
            None => Ok(None),
        }
    }
}
