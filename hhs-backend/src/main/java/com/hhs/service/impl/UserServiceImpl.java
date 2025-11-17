package com.hhs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hhs.common.ErrorCode;
import com.hhs.common.PageResult;
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
import com.hhs.entity.LikeRecord;
import com.hhs.mapper.HealthTipMapper;
import com.hhs.mapper.CollectMapper;
import com.hhs.mapper.CommentMapper;
import com.hhs.mapper.LikeRecordMapper;
import com.hhs.service.HealthTipService;

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
    private final LikeRecordMapper likeRecordMapper;
    private final HealthTipService healthTipService;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                           HealthTipMapper healthTipMapper, CollectMapper collectMapper, 
                           CommentMapper commentMapper, LikeRecordMapper likeRecordMapper,
                           HealthTipService healthTipService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.healthTipMapper = healthTipMapper;
        this.collectMapper = collectMapper;
        this.commentMapper = commentMapper;
        this.likeRecordMapper = likeRecordMapper;
        this.healthTipService = healthTipService;
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
    public UserPublicProfileVO getPublicProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        List<HealthTip> tips = healthTipMapper.selectList(
                Wrappers.<HealthTip>lambdaQuery()
                        .eq(HealthTip::getUserId, userId)
                        .eq(HealthTip::getStatus, 1)
                        .orderByDesc(HealthTip::getPublishTime)
        );

        int publishCount = tips.size();
        int likeCount = tips.stream().mapToInt(HealthTip::getLikeCount).sum();
        int collectCount = tips.stream().mapToInt(HealthTip::getCollectCount).sum();

        List<TipListItemVO> recentTips = tips.stream()
                .limit(10)
                .map(this::toTipListItemVO)
                .toList();

        UserStatsVO stats = UserStatsVO.builder()
                .publishCount(publishCount)
                .likeCount(likeCount)
                .collectCount(collectCount)
                .build();

        return UserPublicProfileVO.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .stats(stats)
                .recentTips(recentTips)
                .build();
    }

    @Override
    public UserProfileVO getUserProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        // 统计数据（使用SQL聚合查询，不加载全量数据）
        // 1. 我发布的技巧数
        int publishCount = healthTipMapper.selectCount(
                Wrappers.<HealthTip>lambdaQuery()
                        .eq(HealthTip::getUserId, userId)
                        .eq(HealthTip::getStatus, 1)
        ).intValue();
        
        // 2. 我的技巧总浏览量
        Integer totalViewsResult = healthTipMapper.selectTotalViewsByUser(userId);
        int totalViews = totalViewsResult != null ? totalViewsResult : 0;
        
        // 3. 我的技巧获得的总点赞数
        Integer totalLikesResult = healthTipMapper.selectTotalLikesByUser(userId);
        int totalLikes = totalLikesResult != null ? totalLikesResult : 0;
        
        // 4. 我收藏的技巧数（有效技巧）
        Integer collectCountResult = collectMapper.countValidCollectsByUser(userId);
        int collectCount = collectCountResult != null ? collectCountResult : 0;
        
        // 5. 我点赞的技巧数（只统计有效技巧）
        Integer likeCountResult = likeRecordMapper.countValidLikesByUser(userId);
        int likeCount = likeCountResult != null ? likeCountResult : 0;
        
        // 6. 我发表的评论数（只统计有效技巧的评论）
        Integer commentCountResult = commentMapper.countValidCommentsByUser(userId);
        int commentCount = commentCountResult != null ? commentCountResult : 0;

        UserStatsVO stats = UserStatsVO.builder()
                .publishCount(publishCount)
                .totalViews(totalViews)
                .totalLikes(totalLikes)
                .collectCount(collectCount)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .build();

        return UserProfileVO.builder()
                .profile(toVO(user))
                .stats(stats)
                .publishList(List.of())  // 列表数据通过分页接口获取
                .collectList(List.of())
                .likeList(List.of())
                .commentList(List.of())
                .build();
    }

    @Override
    public PageResult<TipListItemVO> pageUserTips(Long currentUserId, Long userId, int page, int size) {
        return healthTipService.pageUserTips(currentUserId, userId, page, size);
    }

    @Override
    public PageResult<TipListItemVO> pageUserCollects(Long userId, int page, int size) {
        int pageNum = Math.max(page, 1);
        int pageSize = Math.max(1, Math.min(size, 50));

        Page<Collect> pageRequest = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getUserId, userId)
                .orderByDesc(Collect::getCreateTime);

        IPage<Collect> result = collectMapper.selectPage(pageRequest, wrapper);

        List<TipListItemVO> collectList = result.getRecords().stream()
                .map(collect -> {
                    HealthTip tip = healthTipMapper.selectById(collect.getTipId());
                    return (tip != null && tip.getStatus() == 1) ? toTipListItemVO(tip) : null;
                })
                .filter(Objects::nonNull)
                .toList();

        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), collectList);
    }

    @Override
    public PageResult<TipListItemVO> pageUserLikes(Long userId, int page, int size) {
        int pageNum = Math.max(page, 1);
        int pageSize = Math.max(1, Math.min(size, 50));

        Page<LikeRecord> pageRequest = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getTargetType, 1)
                .orderByDesc(LikeRecord::getCreateTime);

        IPage<LikeRecord> result = likeRecordMapper.selectPage(pageRequest, wrapper);

        List<TipListItemVO> likeList = result.getRecords().stream()
                .map(likeRecord -> {
                    HealthTip tip = healthTipMapper.selectById(likeRecord.getTargetId());
                    return (tip != null && tip.getStatus() == 1) ? toTipListItemVO(tip) : null;
                })
                .filter(Objects::nonNull)
                .toList();

        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), likeList);
    }

    @Override
    public PageResult<CommentVO> pageUserComments(Long userId, int page, int size) {
        int pageNum = Math.max(page, 1);
        int pageSize = Math.max(1, Math.min(size, 50));

        Page<Comment> pageRequest = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getUserId, userId)
                .orderByDesc(Comment::getCreateTime);

        IPage<Comment> result = commentMapper.selectPage(pageRequest, wrapper);
        User user = userMapper.selectById(userId);

        List<CommentVO> commentList = result.getRecords().stream()
                .map(comment -> {
                    HealthTip tip = healthTipMapper.selectById(comment.getTipId());
                    if (tip == null || tip.getStatus() == 0) {
                        return null;
                    }
                    
                    return CommentVO.builder()
                            .id(comment.getId())
                            .userId(comment.getUserId())
                            .username(user != null ? user.getNickname() : "未知用户")
                            .avatar(user != null ? user.getAvatar() : null)
                            .content(comment.getContent())
                            .likeCount(comment.getLikeCount())
                            .createTime(comment.getCreateTime())
                            .parentId(comment.getParentId())
                            .tipTitle(tip.getTitle())
                            .tipId(comment.getTipId())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), commentList);
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
        String content = tip.getContent();
        String summary = (content != null && content.length() > 100) 
                ? content.substring(0, 100) + "..." 
                : (content != null ? content : "");
        return TipListItemVO.builder()
                .id(tip.getId())
                .title(tip.getTitle())
                .category(tip.getCategory())
                .summary(summary)
                .viewCount(tip.getViewCount())
                .likeCount(tip.getLikeCount())
                .collectCount(tip.getCollectCount())
                .author(user != null ? user.getNickname() : "未知用户")
                .authorAvatar(user != null ? user.getAvatar() : null)
                .publishTime(tip.getPublishTime())
                .build();
    }
}
