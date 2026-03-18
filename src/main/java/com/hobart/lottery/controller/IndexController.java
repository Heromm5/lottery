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
     * 处理根路径 /
     */
    @GetMapping("/")
    public String root() {
        return "forward:/index.html";
    }

    /**
     * 防止刷新 404 - 重定向所有非静态资源请求到 index.html
     * 只匹配不包含点（.）的路径，避免与静态资源冲突
     */
    @GetMapping(value = "/{path:[^\\.]+}")
    public String spaForward() {
        return "forward:/index.html";
    }
}
