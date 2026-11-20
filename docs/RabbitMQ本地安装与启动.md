# RabbitMQ 本地安装与启动（campus-trade）

## 现状（2026-11-19 已跑通）

- Erlang OTP **26.2.5**：`D:\Rabbitmq\erlang`（`erts-14.2.5`）
- RabbitMQ **3.13.7**：`D:\Rabbitmq\server\rabbitmq_server-3.13.7`
- 端口：4369（epmd 名称服务）、5672（AMQP 协议端口）、15672（Web 管理台）
- 管理台 <http://localhost:15672>，账号 `guest` / `guest`（只允许本机访问）
- 已验证：`RabbitMQ 3.13.7 + Erlang 26.2.5 [jit]`，`Starting broker... completed with 3 plugins`

## 之前为什么一直崩

报错关键词：`{shutdown,{failed_to_start_child,net_kernel,{'EXIT',nodedistribution}}}` + `Runtime terminating during boot`

RabbitMQ 启动第一步是把 Erlang 自己注册成一个集群节点（`rabbit@localhost`），负责登记名字的是 **epmd**（4369 端口，随 Erlang 附带的小程序）。epmd 没起来 → 节点注册失败 → 整个 VM 退出。

**解法：先确保 epmd 在跑，再启动 RabbitMQ。** 启动脚本已经内置了这一步。

## 启动方式

双击 `D:\idea\campus-trade\start-rabbitmq.bat`

脚本做的事：
1. 设置 `ERLANG_HOME` / `PATH` / `RABBITMQ_NODENAME=rabbit@localhost`（不依赖系统环境变量）
2. 检查 `epmd.exe` 是否在跑，没跑就 `start /b epmd.exe -daemon` 并等 3 秒
3. 写 `%APPDATA%\RabbitMQ\enabled_plugins`（启用 15672 管理台；直接写文件，不调 `rabbitmq-plugins`，避免它傻等正在运行的节点）
4. `call rabbitmq-server.bat`

看到 `Starting broker... completed with N plugins` 就算起来。**黑窗口别关**，关了服务就停。

## 常用命令（都在 sbin 目录下）

```bat
rabbitmqctl.bat status                     :: 看节点状态
rabbitmqctl.bat list_queues name messages  :: 看队列和堆积
rabbitmq-plugins.bat list                  :: 看已启用插件
```

## 常见坑

| 现象 | 原因 | 处理 |
|---|---|---|
| `nodedistribution` / `nodedown` | epmd 没起来 | 先 `epmd.exe -daemon`，再起服务（bat 已处理） |
| 15672 打不开但 5672 通 | 管理插件没启用 | 检查 `%APPDATA%\RabbitMQ\enabled_plugins` 是否有 `[rabbitmq_management].` |
| 窗口卡在 "Enable management plugin" | 用了 `rabbitmq-plugins enable`（它要连正在运行的节点） | 改用 `--offline` 或直接写 enabled_plugins 文件 |
| `where erl` 找不到 | 已开着的终端沿用旧环境变量 | 关掉重开终端 |
| 服务随窗口关闭而停止 | zip 版不是 Windows 服务 | 需要常驻就装服务：`rabbitmq-service.bat install` + `start`（管理员） |

## 上云后

云服务器（Ubuntu）上用 Docker，一条命令即可，不需要这些 Windows 依赖：

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.13-management
```
