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

## 每日工作台（`/board`）

前台多了一个 `/board` 页面，把「每天发生的事」汇总在一起：GitHub 今日提交、arXiv 论文雷达、
AI 撰写的每日汇报、Hermes 对话摘要、本机软件与文件操作、历史对话检索。
它复用博客自身的样式变量，所以明暗主题下都和站点是同一套观感。

数据不经过后端，而是来自本机的一个零依赖看板服务：

- 看板服务：`G:\code\github\workbench\app.py`（纯 Python 标准库，监听 `127.0.0.1:3300`）
- 前端通过 Vite 代理 `/board-api` → `http://127.0.0.1:3300` 读取，避免跨域
- 部署到服务器时用 nginx 做同样反代即可，或设 `VITE_BOARD_BASE` 直接指向看板地址

因此本地需要同时跑三样东西：看板服务（3300）+ 后端（8080）+ 前端（5173）。
Windows 下双击 `start-blog.bat` 一键全部启动，`stop-blog.bat` 全部停止。
看板服务没开时 `/board` 会给出提示，其余页面不受影响。

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
