package com.wangji92.target.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 汪小哥
 * @date 30-01-2020
 */
@RestController
@RequestMapping("/")
public class JavaAgentTargetController {

    @RequestMapping("/test/{name}")
    @ResponseBody
    public String getClassName(@PathVariable String name) {
        String result = this.getClass().getSimpleName().concat(name);
        return result;
    }
}
