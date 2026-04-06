# AGENTS Guidelines for This Repository

TUTnextAPP-android 是多摩大学非官方校园生活应用 TUTnext 的 Android 移植版。  
iOS 原版位于 `/Users/meikenn/Documents/Github/TUTnextApp`，基于 SwiftUI + MVVM。

---

## 1. Project Specifications
- **Minimum SDK:** 24
- **Target SDK:** 34
- **Language:** Kotlin 1.9+
- **Build System:** Gradle Kotlin DSL
- **Package:** `com.meikenn.tama`

## 2. Architecture & Design Patterns

遵循 Android 官方架构指南，与 iOS 版保持功能一致。

```
app/src/main/java/com/meikenn/tama/
├── di/                  # Hilt modules
├── data/
│   ├── local/           # Room DB, DataStore, EncryptedSharedPreferences
│   ├── remote/          # Retrofit API definitions
│   ├── repository/      # Repository implementations
│   └── model/           # API request/response DTOs
├── domain/
│   ├── model/           # Domain entities (Course, Assignment, BusSchedule, User...)
│   └── usecase/         # Business logic (optional, for complex flows)
├── ui/
│   ├── theme/           # Material3 主题
│   ├── navigation/      # Navigation Compose 路由
│   ├── component/       # 可复用 Compose 组件
│   └── feature/
│       ├── login/       # 登录
│       ├── timetable/   # 时间割
│       ├── coursedetail/ # 课程详情 (公告/出勤/教学大纲)
│       ├── assignment/  # 课题
│       ├── bus/         # 校巴时刻表
│       ├── teacher/     # 教员邮件列表
│       ├── print/       # 印刷系统
│       └── settings/    # 设置
└── util/                # Extensions, constants
```

**关键原则：**
- Presentation: StateFlow + Jetpack Compose, UDF (Unidirectional Data Flow)
- Data: Repository pattern (Room + Retrofit)
- DI: Hilt
- Concurrency: Kotlin Coroutines/Flow（注入 Dispatcher）

## 3. iOS → Android 技术映射

| iOS | Android |
|-----|---------|
| SwiftUI | Jetpack Compose |
| @Observable ViewModel | @HiltViewModel + StateFlow |
| SwiftData | Room Database |
| Keychain | EncryptedSharedPreferences |
| UserDefaults | Preferences DataStore |
| Combine | Kotlin Flow |
| URLSession (APIService) | Retrofit + OkHttp |
| Cookie 管理 | OkHttp CookieJar |
| APNs 推送 | 暂不移植 |
| WidgetKit | 暂不移植 |
| Live Activities | 暂不移植 |
| NFC 学生証读取 | 暂不移植 |
| Deep Link (tama://) | Android Deep Links |
| SafariViewController | Chrome Custom Tabs |
| MFMailComposeViewController | Intent.ACTION_SENDTO |

## 4. API 端点 (与 iOS 完全一致)

所有请求均为 POST + JSON：

| 端点 | 用途 |
|------|------|
| `https://next.tama.ac.jp/uprx/webapi/up/pk/Pky001Resource/login` | 登录 |
| `https://next.tama.ac.jp/uprx/webapi/up/pk/Pky002Resource/logout` | 登出 |
| `https://next.tama.ac.jp/uprx/webapi/up/ap/Apa001Resource/firstSetting` | 首次设置 |
| `https://next.tama.ac.jp/uprx/webapi/up/ap/Apa004Resource/getJugyoKeijiMenuInfo` | 时间割 |
| `https://next.tama.ac.jp/uprx/webapi/up/ap/Apa005Resource/getJugyoDetail` | 课程详情 |
| `https://tama.qaq.tw/kadai` | 课题列表 |
| `https://tama.qaq.tw/bus/app_data` | 校巴时刻表 |
| `https://tama.qaq.tw/push/send` | 注册推送 Token |
| `https://cloudodp.fujifilm.com/guestweb/*` | 印刷系统 |

**请求格式（统一 DTO）：**
```json
{
  "productCd": "ap",
  "subProductCd": "apa",
  "loginUserId": "USERNAME",
  "encryptedLoginPassword": "ENCRYPTED_PWD",
  "langCd": "ja",
  "data": {}
}
```

## 5. 数据持久化策略

| 数据类型 | 存储方式 | 过期策略 |
|---------|---------|---------|
| 用户凭证 (User + encryptedPassword) | EncryptedSharedPreferences | 登出时清除 |
| 时间割缓存 | Room (JSON blob) | 12 小时 |
| 校巴缓存 | Room (JSON blob) | 12 小时 |
| 教室变更记录 | Room (单行) | 按 expiryDate |
| 课程颜色偏好 | Room (jugyoCd → colorIndex) | 永久 |
| 打印记录 | Room (PrintUploadRecord) | 按 expiryDate |
| 应用偏好 (暗色模式等) | Preferences DataStore | 永久 |
| 课题指纹 (去重) | Preferences DataStore | 永久 |

## 6. 移植阶段规划

详见 [MIGRATION_PLAN.md](MIGRATION_PLAN.md)。所有 6 个 Phase 已完成实现。

## 7. 编码行动准则

1. **功能对等优先**：以 iOS 版为参考，确保每个功能的行为和数据流一致。实现前先阅读对应的 iOS 源文件。
2. **逐 Phase 推进**：按上述阶段顺序开发，每完成一个 Phase 确保可编译运行。
3. **API 请求完全复用**：端点、请求体、响应解析逻辑与 iOS 一致，参考 iOS 的 `APIService.swift`、各 Service 文件。
4. **缓存策略一致**：12 小时 TTL，先返回缓存再后台刷新，与 iOS 行为一致。
5. **不移植的功能**：Live Activities、WidgetKit、NFC 学生证读取、推送通知 (FCM) 均暂不实现。
6. **不引入不必要的抽象**：如果一个 UseCase 只是透传 Repository，直接让 ViewModel 调用 Repository。
7. **参考而非翻译**：不要逐行翻译 Swift 代码，而是理解业务逻辑后用 Kotlin/Android 惯用方式重写。
8. **测试跟随功能**：每个 ViewModel 和 Repository 要有对应的单元测试。

## 8. Testing Philosophy
- **Unit Tests:** JUnit4, MockK, Turbine for Flow
- **UI Tests:** Compose Test Rule
- 优先测试 ViewModel 的 StateFlow 状态变化

## 9. 参考资料
- iOS 源码: `/Users/meikenn/Documents/Github/TUTnextApp/tama/`
- [Android Developer Documentation](https://developer.android.com)
- [Kotlin Documentation](https://kotlinlang.org)

---

*每完成一个 Phase 后更新此文件中对应的 checkbox。*
