package com.hhs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hhs.common.ErrorCode;
import com.hhs.dto.ChangePasswordRequest;
import com.hhs.dto.LoginRequest;
import com.hhs.dto.RegisterRequest;
import com.hhs.dto.UpdateProfileRequest;
import com.hhs.entity.User;
import com.hhs.exception.BusinessException;
import com.hhs.mapper.UserMapper;
import com.hhs.security.JwtUtil;
import com.hhs.service.UserService;
import com.hhs.vo.*;
import com.hhs.entity.HealthTip;
import com.hhs.entity.Collect;
import com.hhs.entity.Comment;
import com.hhs.mapper.HealthTipMapper;
import com.hhs.mapper.CollectMapper;
import com.hhs.mapper.CommentMapper;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final HealthTipMapper healthTipMapper;
    private final CollectMapper collectMapper;
    private final CommentMapper commentMapper;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                           HealthTipMapper healthTipMapper, CollectMapper collectMapper, CommentMapper commentMapper) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.healthTipMapper = healthTipMapper;
        this.collectMapper = collectMapper;
        this.commentMapper = commentMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request) {
        User exists = userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, request.username())
                .last("LIMIT 1"));
        if (exists != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }
        if (StringUtils.hasText(request.email())) {
            long count = userMapper.selectCount(Wrappers.<User>lambdaQuery()
                    .eq(User::getEmail, request.email()));
            if (count > 0) {
                throw new BusinessException(ErrorCode.CONFLICT, "邮箱已被注册");
            }
        }
        User user = new User();
        user.setUsername(request.username());
        user.setNickname(request.nickname());
        user.setEmail(request.email());
        user.setStatus(1);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setAvatar("https://api.dicebear.com/7.x/identicon/svg?seed=" + request.username());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.username())
                .last("LIMIT 1"));
        if (user == null || user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return AuthResponse.builder()
                .token(token)
                .userInfo(toVO(user))
                .build();
    }

    @Override
    public UserVO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return toVO(user);
    }

    @Override
    public UserProfileVO getUserProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        // 获取用户发布的技巧列表
        List<HealthTip> publishedTips = healthTipMapper.selectList(
                Wrappers.<HealthTip>lambdaQuery()
                        .eq(HealthTip::getUserId, userId)
                        .orderByDesc(HealthTip::getPublishTime)
        );
        List<TipListItemVO> publishList = publishedTips.stream()
                .map(this::toTipListItemVO)
                .toList();

        // 获取用户收藏的技巧列表
        List<Collect> collects = collectMapper.selectList(
                Wrappers.<Collect>lambdaQuery()
                        .eq(Collect::getUserId, userId)
                        .orderByDesc(Collect::getCreateTime)
        );
        List<TipListItemVO> collectList = collects.stream()
                .map(collect -> {
                    HealthTip tip = healthTipMapper.selectById(collect.getTipId());
                    return tip != null ? toTipListItemVO(tip) : null;
                })
                .filter(Objects::nonNull)
                .toList();

        // 获取用户的评论列表
        List<Comment> comments = commentMapper.selectList(
                Wrappers.<Comment>lambdaQuery()
                        .eq(Comment::getUserId, userId)
                        .orderByDesc(Comment::getCreateTime)
                        .last("LIMIT 50")  // 限制最多返回50条评论
        );
        List<CommentVO> commentList = comments.stream()
                .map(comment -> {
                    HealthTip tip = healthTipMapper.selectById(comment.getTipId());
                    String tipTitle = tip != null ? tip.getTitle() : "已删除的技巧";
                    
                    return CommentVO.builder()
                            .id(comment.getId())
                            .userId(comment.getUserId())
                            .username(user.getNickname())
                            .avatar(user.getAvatar())
                            .content(comment.getContent())
                            .likeCount(comment.getLikeCount())
                            .createTime(comment.getCreateTime())
                            .parentId(comment.getParentId())
                            .tipTitle(tipTitle)  // 添加技巧标题便于前端显示
                            .tipId(comment.getTipId())  // 添加技巧ID用于跳转
                            .build();
                })
                .toList();

        // 统计数据
        int likeCount = publishedTips.stream()
                .mapToInt(HealthTip::getLikeCount)
                .sum();

        UserStatsVO stats = UserStatsVO.builder()
                .publishCount(publishList.size())
                .likeCount(likeCount)
                .collectCount(collectList.size())
                .build();

        return UserProfileVO.builder()
                .profile(toVO(user))
                .stats(stats)
                .publishList(publishList)
                .collectList(collectList)
                .commentList(commentList)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (StringUtils.hasText(request.email()) && !Objects.equals(request.email(), user.getEmail())) {
            long count = userMapper.selectCount(Wrappers.<User>lambdaQuery()
                    .eq(User::getEmail, request.email())
                    .ne(User::getId, userId));
            if (count > 0) {
                throw new BusinessException(ErrorCode.CONFLICT, "邮箱已被使用");
            }
            user.setEmail(request.email());
        }
        if (StringUtils.hasText(request.nickname())) {
            user.setNickname(request.nickname());
        }
        if (StringUtils.hasText(request.avatar())) {
            user.setAvatar(request.avatar());
        }
        if (StringUtils.hasText(request.phone())) {
            user.setPhone(request.phone());
        }
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "旧密码不正确");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    private UserVO toVO(User user) {
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }

    private TipListItemVO toTipListItemVO(HealthTip tip) {
        User user = userMapper.selectById(tip.getUserId());
        return TipListItemVO.builder()
                .id(tip.getId())
                .title(tip.getTitle())
                .category(tip.getCategory())
                .summary(tip.getContent().length() > 100 ? tip.getContent().substring(0, 100) + "..." : tip.getContent())
                .viewCount(tip.getViewCount())
                .likeCount(tip.getLikeCount())
                .collectCount(tip.getCollectCount())
                .author(user != null ? user.getNickname() : "未知用户")
                .authorAvatar(user != null ? user.getAvatar() : null)
                .publishTime(tip.getPublishTime())
                .build();
    }
}
