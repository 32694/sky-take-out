# 🍔 外卖点单系统

一个基于 **SpringBoot** 开发的外卖点单系统，包含用户端与商家端功能，支持点餐、下单、订单管理等操作。  
本项目用于学习和展示 Web 后端开发流程。

## 🧰 技术栈

### 后端
- Spring Boot
- MySQL
- MyBatis
- Redis

### 前端
- Vue.js
- Element UI

## 💡 主要功能

- 用户注册登录
- 菜品浏览搜索
- 购物车管理
- 在线下单支付
- 订单状态跟踪
- 菜品管理
- 订单管理
- 销售统计报表
- 热门菜品排行

## 📂 项目结构

```
sky-take-out/
├─ sky-common/ # 公共模块（常量、工具类等）
├─ sky-pojo/ # 数据实体与传输对象
├─ sky-server/ # 服务端主程序（控制层、业务逻辑、配置等）
└─ README.md
```

## 🚀 快速开始

### 环境要求
- JDK 8+
- MySQL 5.7+
- Maven 3.6+

### 安装步骤

1. 克隆项目
```bash
git clone https://github.com/32694/sky-take-out.git
cd sky-take-out
```

2. 数据库配置
创建数据库并导入SQL脚本

3. 修改配置文件
```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sky_take_out
    username: your_username
    password: your_password
```

4. 启动项目
```bash
cd sky-server
mvn spring-boot:run
```

5. 访问应用
- 后端API: http://localhost:8080
- API文档: http://localhost:8080/doc.html

## ⚙️ 配置说明

主要配置项：
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sky_take_out
    username: root
    password: 1234
```



## ✉️ 联系方式

- 项目作者: 樊怡 13213599216@163.com
- 项目地址: https://github.com/32694/sky-take-out
