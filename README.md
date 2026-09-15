# 个人博客管理系统

一个 Spring Boot + Vue 3 的前后端小项目，包含：

- 前台博客首页、文章详情、分类筛选
- 后台登录、文章管理、分类管理
- **每日工作台**（`/board`）：把 GitHub 今日提交、arXiv 论文雷达、AI 每日汇报、对话摘要和本机操作记录汇总成一页
- Spring Boot + JPA + MySQL
- Vue 3 + Vite + Vue Router + Axios

## 目录

- `backend/` 后端
- `frontend/` 前端
- `start-blog.bat` / `stop-blog.bat` 本地一键启停（Windows）

## 启动

后端：

```bash
cd backend
mvn spring-boot:run
```

本地如果想临时用 H2，可以加 `local` 配置：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

前端：

```bash
cd frontend
npm install
npm run dev
```

后台默认账号：

- 用户名：`admin`
- 密码：`admin123`

## 每日工作台（`/board`）与知识库（`/kb`）

除了博客本身，前台还有两个「个人仪表」页面，都复用博客自身的样式变量，
所以明暗主题下都和站点是同一套观感：

| 页面 | 内容 | 数据来自 |
| --- | --- | --- |
| `/board` | GitHub 今日提交、arXiv 论文雷达、AI 每日汇报、对话摘要、本机软件与文件操作、历史对话检索 | 本机看板服务 `127.0.0.1:3300` |
| `/kb` | 笔记列表、文件夹/标签筛选、全文检索、正文阅读（含 `[[双链]]` 跳转）、反向链接 | 本机 AIO·知识库 `127.0.0.1:3200` |

两个数据源都是本机服务，前端一律走 **Vite 同源代理**，因此浏览器端不存在跨域问题：

- `/board-api` → `http://127.0.0.1:3300`（看板：`G:\code\github\workbench\app.py`）
- `/kb-api` → `http://127.0.0.1:3200`（知识库：`G:\code\knowledge\aio-kb\server.js`，含它的 `/lib/*`，
  所以博客直接复用了知识库自带的 marked / DOMPurify，没有新增 npm 依赖）

部署到服务器时用 nginx 写同样的反代即可，或设 `VITE_BOARD_BASE` / `VITE_KB_BASE` 指向对应地址。

因此本地需要同时跑四样东西：知识库（3200）+ 看板（3300）+ 后端（8080）+ 前端（5173）。
Windows 下双击 `start-blog.bat` 一键全部启动，`stop-blog.bat` 全部停止。
某个本机服务没开时，对应页面会显示一句提示，其余页面不受影响。

## Docker 部署

复制一份环境变量文件：

```bash
cp .env.example .env
```

然后改掉里面的密码，接着启动：

```bash
docker compose up -d --build
```

访问地址：

- 前台：`http://你的服务器IP/`
- 后台：`http://你的服务器IP/admin/login`

默认暴露的是 `80` 端口，MySQL 只在容器内使用。

## 腾讯云上线

服务器建议放行：

- `22` 端口
- `80` 端口
- `443` 端口

Ubuntu 服务器上如果还没装 Docker，可以直接执行：

```bash
curl -fsSL https://get.docker.com | sudo sh
sudo systemctl enable --now docker
sudo usermod -aG docker $USER
```

重新登录一次 SSH 后，再进入项目目录执行 `docker compose up -d --build`。

## 本地环境

当前后端已降到 `Spring Boot 2.7`，可以使用 `Java 8` 启动。  
项目默认使用 `MySQL`，请先创建数据库 `blogdb`，或者通过环境变量指定：

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

已验证的本地环境：

- Java 8
- Maven 3.6.1
- Node.js 24
- MySQL 8
