# 个人博客管理系统

一个 Spring Boot + Vue 3 的前后端小项目，包含：

- 前台博客首页、文章详情、分类筛选
- 后台登录、文章管理、分类管理
- Spring Boot + JPA + H2
- Vue 3 + Vite + Vue Router + Axios

## 目录

- `backend/` 后端
- `frontend/` 前端

## 启动

后端：

```bash
cd backend
mvn spring-boot:run
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

## 本地环境

当前后端已降到 `Spring Boot 2.7`，可以使用 `Java 8` 启动。  
项目默认使用 `H2` 内存数据库，不需要 MySQL 也可以先跑通。

已验证的本地环境：

- Java 8
- Maven 3.6.1
- Node.js 24
- MySQL 8
