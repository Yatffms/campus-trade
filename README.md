# campus-trade 校园二手交易平台

一个用于学习与求职展示的 Java 后端项目。

## 技术栈

- JDK 17
- Spring Boot 3.3.5
- MyBatis-Plus 3.5.7
- MySQL 8.0
- Maven 3.9.16

## 启动前准备

1. 启动 MySQL 服务（MySQL80）。
2. 创建数据库 `campus_trade`（utf8mb4）。
3. 修改 `src/main/resources/application.yml` 里的 `password` 为你的 MySQL 密码。
4. 在 IDEA 中打开本目录，等待 Maven 依赖下载完成。

## 运行

```bash
mvn spring-boot:run
```

或直接运行 `CampusTradeApplication`。

默认端口：`http://localhost:8080`
