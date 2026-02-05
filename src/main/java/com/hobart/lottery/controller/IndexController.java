package com.hobart.lottery.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * SPA 入口控制器
 * 将根路径请求转发到静态资源目录的 index.html
 */
@Controller
public class IndexController {

    /**
     * SPA 入口 - 返回静态 index.html
     * Spring Boot 默认会从 classpath:/static/ 或 /public 目录查找
     */
    @GetMapping("/")
    public String index() {
        // 由于 index.html 已在 static 目录，Spring Boot 会自动返回
        // 此处返回 "forward:index.html" 让 Spring 转发请求
        return "forward:/index.html";
    }

    /**
     * 防止刷新 404 - 转发所有非 API 请求到 index.html
     * 注意：此配置已由 Spring Boot 默认处理
     * 如需更复杂的路由控制，可使用 WebMvcConfigurer 覆盖
     */
}
