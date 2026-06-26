package com.quiz.app.service;

import com.quiz.app.dto.*;
import java.util.List;

public interface WxAuthService {
    LoginResponse login(LoginRequest request);
}
