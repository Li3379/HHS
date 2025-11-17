package com.hhs.service;

import com.hhs.common.PageResult;
import com.hhs.dto.ChangePasswordRequest;
import com.hhs.dto.LoginRequest;
import com.hhs.dto.RegisterRequest;
import com.hhs.dto.UpdateProfileRequest;
import com.hhs.vo.AuthResponse;
import com.hhs.vo.CommentVO;
import com.hhs.vo.TipListItemVO;
import com.hhs.vo.UserProfileVO;
import com.hhs.vo.UserPublicProfileVO;
import com.hhs.vo.UserVO;

public interface UserService {

    void register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserProfileVO getUserProfile(Long userId);

    UserPublicProfileVO getPublicProfile(Long userId);

    void updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    PageResult<TipListItemVO> pageUserTips(Long currentUserId, Long userId, int page, int size);

    PageResult<TipListItemVO> pageUserCollects(Long userId, int page, int size);

    PageResult<TipListItemVO> pageUserLikes(Long userId, int page, int size);

    PageResult<CommentVO> pageUserComments(Long userId, int page, int size);
}
