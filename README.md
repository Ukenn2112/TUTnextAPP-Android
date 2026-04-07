# TUTnext for Android

多摩大学非公式キャンパスライフアプリ **TUTnext** の Android 版です。

iOS 版 ([TUTnextApp](https://github.com/Ukenn2112/TUTnextApp)) を参考に、Kotlin / Jetpack Compose でネイティブ実装しています。

## 機能

- **時間割** — 授業一覧と時限ごとの表示
- **授業詳細** — お知らせ・出席状況・シラバス
- **課題一覧** — 提出期限付きの課題管理
- **スクールバス時刻表** — リアルタイムカウントダウン付き
- **教員メールリスト** — ワンタップでメール送信
- **印刷システム** — クラウドプリント連携
- **設定** — ダークモード、キャッシュ管理など

## 技術スタック

| カテゴリ | 技術 |
|---------|------|
| 言語 | Kotlin 1.9+ |
| UI | Jetpack Compose + Material 3 |
| アーキテクチャ | MVVM / UDF (StateFlow) |
| DI | Hilt |
| ネットワーク | Retrofit + OkHttp |
| ローカル DB | Room |
| 認証情報保存 | EncryptedSharedPreferences |
| 設定保存 | Preferences DataStore |
| 非同期処理 | Kotlin Coroutines / Flow |
| テスト | JUnit 4, MockK, Turbine |

## 必要環境

- Android Studio Hedgehog 以降
- JDK 17
- Android SDK 34
- 最低対応: Android 7.0 (API 24)

## ビルド方法

```bash
git clone https://github.com/Ukenn2112/TUTnextAPP-android.git
cd TUTnextAPP-android
./gradlew assembleDebug
```

## プロジェクト構成

```
app/src/main/java/com/meikenn/tama/
├── di/              # Hilt モジュール
├── data/
│   ├── local/       # Room DB, DataStore
│   ├── remote/      # Retrofit API 定義
│   ├── repository/  # Repository 実装
│   └── model/       # API リクエスト/レスポンス DTO
├── domain/
│   ├── model/       # ドメインエンティティ
│   └── usecase/     # ビジネスロジック
├── ui/
│   ├── theme/       # Material 3 テーマ
│   ├── navigation/  # Navigation Compose ルーティング
│   ├── component/   # 共通 Compose コンポーネント
│   └── feature/     # 画面ごとの機能モジュール
└── util/            # 拡張関数、定数
```

## ライセンス

This project is for personal and educational use only.

## 作者

[@Ukenn2112](https://github.com/Ukenn2112)
