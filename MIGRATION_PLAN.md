# 移植阶段规划

TUTnext iOS → Android 移植计划。每完成一个 Phase 后更新对应的 checkbox。

---

## Phase 1: 基础框架 + 登录
- [x] Hilt DI 模块 (NetworkModule, DatabaseModule, AuthModule)
- [x] Retrofit + OkHttp 配置 (CookieJarImpl, HeaderInterceptor, 日志拦截器)
- [x] 统一请求/响应 DTO (ApiRequestBody, ApiResponse)
- [x] EncryptedSharedPreferences (SecureStorage - 用户凭证)
- [x] Room Database + 缓存表 (5 entities, 5 DAOs)
- [x] DataStore Preferences (PreferencesManager)
- [x] AuthRepository (login / logout / firstSetting)
- [x] LoginScreen + LoginViewModel

## Phase 2: 时间割 (核心功能)
- [x] TimetableRepository (API + Room 缓存, 12h TTL, 3次重试)
- [x] Course 域模型 (weekdayString, periodInfo 计算属性)
- [x] TimetableScreen (网格展示, 7限×6日)
- [x] CourseColorService (Room 持久化, 11色预设)
- [x] RoomChange 跟踪 (48h 过期自动清理)
- [x] Semester 管理 (DataStore 持久化)

## Phase 3: 课程详情
- [x] CourseDetailRepository (公告/出勤/备忘录)
- [x] CourseDetailScreen (公告列表, 出勤统计条, 备忘录编辑)
- [x] 导航集成 (从时间割点击课程→详情页)

## Phase 4: 课题管理
- [x] AssignmentRepository (API + 指纹去重)
- [x] AssignmentScreen + AssignmentCard
- [x] 筛选功能 (全て/今日/今週/今月/期限切れ)
- [x] 剩余时间倒计时 (每分钟刷新)

## Phase 5: 校巴时刻表
- [x] BusScheduleRepository (API + Room 缓存, 12h TTL)
- [x] BusScheduleScreen (路线/时刻/特殊注释)
- [x] 4 路线 × 3 时刻类型
- [x] 当前时间高亮 + 下一班倒计时
- [x] 临时消息 + Pin消息显示

## Phase 6: 辅助功能
- [x] 主导航框架 (底部4Tab: バス/時間割/課題/その他)
- [x] MainScaffold (TopAppBar + BottomNavigation + MoreMenu)
- [x] TeacherEmailListScreen (教员目录, 50音分组, 邮件Intent)
- [x] PrintSystemScreen (文件上传到 Fujifilm CloudODP)
- [x] SettingsScreen (暗色模式, 用户信息, 登出)
- [x] DarkModeSettingsScreen (System/Light/Dark)

## 暂不移植
以下功能暂不纳入移植范围：
- Live Activities
- WidgetKit (Glance App Widget)
- NFC 学生証读取
- 推送通知 (FCM)
- Deep Link 路由
