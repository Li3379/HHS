package com.hhs.service;

import com.hhs.dto.ChangePasswordRequest;
import com.hhs.dto.LoginRequest;
import com.hhs.dto.RegisterRequest;
import com.hhs.dto.UpdateProfileRequest;
import com.hhs.vo.AuthResponse;
import com.hhs.vo.UserProfileVO;
import com.hhs.vo.UserVO;

public interface UserService {

    void register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserVO getProfile(Long userId);

    UserProfileVO getUserProfile(Long userId);

    void updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);
}
