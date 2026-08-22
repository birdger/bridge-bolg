package com.example.blog.service;

import com.example.blog.config.AppProperties;
import com.example.blog.web.BizException;
import com.example.blog.web.dto.LoginRequest;
import com.example.blog.web.dto.LoginResponse;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AppProperties appProperties;

    public AuthService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public LoginResponse login(LoginRequest request) {
        if (!appProperties.getAdmin().getUsername().equals(request.getUsername())
                || !appProperties.getAdmin().getPassword().equals(request.getPassword())) {
            throw new BizException(401, "用户名或密码错误");
        }
        return new LoginResponse(appProperties.getAdmin().getToken(), appProperties.getAdmin().getUsername());
    }
}
