# クワログ (Kuwalog)

昆虫（オオクワガタ等）の譲渡における、生体の累代・親個体情報と
ブリーダーの信用情報を構造化して管理するWebアプリケーション。
転職用ポートフォリオとして開発。

## 技術スタック

- Java 21
- Spring Boot 3.3
- PostgreSQL 16
- Spring Data JPA / Flyway / Thymeleaf / Spring Security

## 必要環境

- JDK 21
- PostgreSQL 16（DB名: `kuwalog`、ユーザー: `postgres`）

## 環境変数

| 変数名 | デフォルト値 | 説明 |
|--------|-------------|------|
| `DB_URL` | `jdbc:postgresql://localhost:5433/kuwalog` | JDBC URL |
| `DB_USERNAME` | `postgres` | DBユーザー名 |
| `DB_PASSWORD` | （必須・デフォルトなし） | DBパスワード |

## 起動手順

```powershell
$env:DB_PASSWORD = "あなたのDBパスワード"
.\gradlew.bat bootRun
```

起動後、`http://localhost:8080/users/register` でユーザー登録できます。

## 実装済み機能

- ユーザー登録・ログイン・ログアウト
- 生体（beetles）の投稿・一覧・詳細・編集・削除
- 譲渡記録（transactions）の登録・一覧・削除

## テーブル設計

`docs/kuwalog_er_diagram.mermaid.txt` を参照。
