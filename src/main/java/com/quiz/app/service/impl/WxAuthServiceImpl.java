package com.quiz.app.service.impl;

import com.quiz.app.dto.*;
import com.quiz.app.entity.WxUser;
import com.quiz.app.mapper.WxUserMapper;
import com.quiz.app.service.WxAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WxAuthServiceImpl implements WxAuthService {
    
    private final WxUserMapper wxUserMapper;
    private final WebClient wxWebClient;
    
    @Value("${wx.app-id}")
    private String appId;
    
    @Value("${wx.app-secret}")
    private String appSecret;
    
    @Override
    public LoginResponse login(LoginRequest request) {
        String url = String.format(
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
            appId, appSecret, request.getCode()
        );
        
        try {
            Map<String, Object> result = wxWebClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            
            String openid = (String) result.get("openid");
            String errmsg = (String) result.get("errmsg");
            
            if (openid == null || openid.isEmpty()) {
                throw new RuntimeException("微信登录失败: " + errmsg);
            }
            
            WxUser user = wxUserMapper.findByOpenid(openid);
            boolean isNewUser = false;
            
            if (user == null) {
                user = new WxUser();
                user.setOpenid(openid);
                wxUserMapper.insert(user);
                isNewUser = true;
                log.info("New user created: {}", openid);
            }
            
            LoginResponse response = new LoginResponse();
            response.setUserId(user.getId());
            response.setOpenid(user.getOpenid());
            response.setNickname(user.getNickname());
            response.setAvatar(user.getAvatar());
            response.setNewUser(isNewUser);
            
            return response;
            
        } catch (Exception e) {
            log.error("Login failed", e);
            throw new RuntimeException("微信登录异常: " + e.getMessage());
        }
    }
}
