# 移植阶段规划

TUTnext iOS → Android 移植计划。每完成一个 Phase 后更新对应的 checkbox。

---

## Phase 1: 基础框架 + 登录
- [ ] Hilt DI 模块 (Network, Database, Auth)
- [ ] Retrofit + OkHttp 配置 (CookieJar, 日志拦截器)
- [ ] 统一请求/响应 DTO
- [ ] EncryptedSharedPreferences (用户凭证)
- [ ] Room Database + 缓存表
- [ ] AuthRepository (login / logout / firstSetting)
- [ ] LoginScreen + LoginViewModel

## Phase 2: 时间割 (核心功能)
- [ ] TimetableRepository (API + Room 缓存, 12h TTL)
- [ ] CourseModel 域模型
- [ ] TimetableScreen (网格展示, 课程颜色)
- [ ] CourseColorService (Room 持久化)
- [ ] RoomChange 跟踪

## Phase 3: 课程详情
- [ ] CourseDetailRepository (公告/出勤)
- [ ] CourseDetailScreen (公告列表, 出勤统计, 教学大纲)
- [ ] Chrome Custom Tabs (浏览器内打开)

## Phase 4: 课题管理
- [ ] AssignmentRepository (API + 指纹去重)
- [ ] AssignmentScreen + AssignmentCardView
- [ ] 本地通知 (截止前 24h/6h/1h)
- [ ] App Badge (通知角标)

## Phase 5: 校巴时刻表
- [ ] BusScheduleRepository (API + Room 缓存)
- [ ] BusScheduleScreen (路线/时刻/特殊注释)
- [ ] 4 路线 × 3 时刻类型

## Phase 6: 辅助功能
- [ ] TeacherEmailListScreen (教员目录, 邮件 Intent)
- [ ] PrintSystemScreen (文件上传到 Fujifilm CloudODP)
- [ ] SettingsScreen (暗色模式, 账户管理)

## 暂不移植
以下功能暂不纳入移植范围：
- Live Activities
- WidgetKit (Glance App Widget)
- NFC 学生証读取
- 推送通知 (FCM)
- Deep Link 路由
