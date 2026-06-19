# クワログ (Kuwalog) - プロジェクト規約

## プロジェクト概要

昆虫(オオクワガタ等)の譲渡における、生体の累代・親個体情報とブリーダーの信用情報を
構造化して管理するWebアプリケーション。転職用ポートフォリオとして開発する。

- 詳細仕様: README.md を参照
- テーブル設計: docs/er_diagram.mermaid を参照(この設計が正。変更時は必ず先に相談すること)

## 技術スタック(変更禁止。追加ライブラリが必要な場合は先に提案・相談すること)

- 言語: Java 21
- フレームワーク: Spring Boot 3.3系
- DB: PostgreSQL 16
- ORM: Spring Data JPA
- テンプレート: Thymeleaf
- 認証: Spring Security(セッションベース)
- バリデーション: Bean Validation
- ビルド: Gradle
- 画像ストレージ: Cloudinary(Service層でインターフェース分離し、ローカル実装と差し替え可能にする)
- DBマイグレーション: Flyway

## パッケージ構成

com.example.kuwalog

├── controller   # 画面遷移・リクエスト処理のみ。ビジネスロジック禁止

├── service      # ビジネスロジック。トランザクション境界はここ

├── repository   # Spring Data JPAのRepositoryインターフェース

├── entity       # JPAエンティティ

├── dto          # 画面とやり取りするフォームクラス・表示用DTO

├── config       # Spring Security等の設定クラス

└── exception    # 独自例外・例外ハンドラ

## コーディング規約

- ControllerからRepositoryを直接呼ばない。必ずService経由にする
- エンティティを直接画面にバインドしない。フォームクラス(dto)を介する
- DB変更は必ずFlywayマイグレーションファイル(src/main/resources/db/migration)で行う。
  ddl-autoはvalidateに固定し、createやupdateにしない
- 命名: クラスはUpperCamelCase、メソッド/変数はlowerCamelCase、
  テーブル/カラムはsnake_case
- 画面のURLは /beetles, /beetles/{id}, /beetles/{id}/edit のようなRESTライクな構成にする
- マジックナンバー・文字列の直書きを避け、enumまたは定数にする
  (例: 性別、ステージ、評価種別はenum)
- コメントは「なぜそうしたか」を書く。「何をしているか」だけのコメントは不要

## ドメインルール(ビジネスロジックの正)

- 生体の編集・削除は投稿者本人のみ可能。他人の生体への操作は403を返す
- 父個体(father_id)にはsex=オスの生体のみ、母個体(mother_id)にはsex=メスの生体のみ選択可能
- 自分自身を父・母に選択することはできない
- 画像は1生体につき最大4枚。代表画像(is_primary=true)は常に1枚以下
- 評価(review)は譲渡記録(transaction)に紐づく。評価者はそのtransactionの
  from_user_idまたはto_user_idのいずれかであること。被評価者は相手側とする
- 同一transactionに対して、同一ユーザーは同一review_typeの評価を1件まで
- review_typeは NORMAL(通常評価) / FOLLOW_UP(後追い評価) の2種類
- 譲渡記録の登録は、その生体の投稿者のみ可能

## セキュリティ

- パスワードはBCryptでハッシュ化
- 未ログインユーザーは閲覧のみ可能。登録・編集・削除・評価・譲渡記録はログイン必須
- CSRF対策はSpring Securityのデフォルトを有効のままにする
- 認可チェックはController層の入口だけでなくService層でも行う

## 開発の進め方(重要)

- 1回の依頼につき1機能のみ実装する。依頼範囲を超えた機能を先回りで実装しない
- 複数ファイルにまたがる実装は、コードを書く前に実装計画を提示し、承認を得てから着手する
- 実装後は必ず「Controller → Service → Repository の処理の流れ」を日本語で解説する
- 既存コードのスタイル・構成に合わせる。勝手なリファクタリングをしない
- エラーが解決しない場合、場当たり的な回避策を重ねず、原因の仮説を説明してから修正する

## テスト・動作確認

- Serviceクラスには単体テスト(JUnit 5)を書く。特にドメインルールの境界
  (他人の生体の編集拒否、父母の性別バリデーション、評価権限)は必ずテストする
- ./gradlew test が通る状態を維持する

## Git運用

- 機能単位でコミットする。動かない状態でコミットしない
- コミットメッセージは日本語で「何をしたか」を1行で書く(例: 生体一覧・詳細画面を追加)

## やらないこと(スコープ外。提案も不要)

決済、チャット、通知、フォロー、いいね、管理者画面、本人確認、通報、
ランキング、表示回数、SPA化(React/Next.js)、メール認証、OAuthログイン