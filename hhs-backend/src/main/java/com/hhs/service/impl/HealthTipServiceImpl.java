package com.hhs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhs.common.ErrorCode;
import com.hhs.common.PageResult;
import com.hhs.dto.TipCreateRequest;
import com.hhs.dto.TipPageQuery;
import com.hhs.entity.Collect;
import com.hhs.entity.Comment;
import com.hhs.entity.DeleteLog;
import com.hhs.entity.HealthTip;
import com.hhs.entity.LikeRecord;
import com.hhs.entity.User;
import com.hhs.exception.BusinessException;
import com.hhs.mapper.CollectMapper;
import com.hhs.mapper.CommentMapper;
import com.hhs.mapper.DeleteLogMapper;
import com.hhs.mapper.HealthTipMapper;
import com.hhs.mapper.LikeRecordMapper;
import com.hhs.mapper.UserMapper;
import com.hhs.service.HealthTipService;
import com.hhs.vo.ActionStateVO;
import com.hhs.vo.TipDetailVO;
import com.hhs.vo.TipListItemVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class HealthTipServiceImpl implements HealthTipService {

    private static final int TARGET_TYPE_TIP = 1;

    private final HealthTipMapper healthTipMapper;
    private final UserMapper userMapper;
    private final LikeRecordMapper likeRecordMapper;
    private final CollectMapper collectMapper;
    private final CommentMapper commentMapper;
    private final DeleteLogMapper deleteLogMapper;

    public HealthTipServiceImpl(HealthTipMapper healthTipMapper,
                                UserMapper userMapper,
                                LikeRecordMapper likeRecordMapper,
                                CollectMapper collectMapper,
                                CommentMapper commentMapper,
                                DeleteLogMapper deleteLogMapper) {
        this.healthTipMapper = healthTipMapper;
        this.userMapper = userMapper;
        this.likeRecordMapper = likeRecordMapper;
        this.collectMapper = collectMapper;
        this.commentMapper = commentMapper;
        this.deleteLogMapper = deleteLogMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTip(Long userId, TipCreateRequest request) {
        HealthTip tip = new HealthTip();
        tip.setUserId(userId);
        tip.setTitle(request.title());
        tip.setContent(request.content());
        tip.setCategory(request.category());
        tip.setTags(String.join(",", request.tags()));
        tip.setSummary(buildSummary(request));
        tip.setStatus(1);
        tip.setViewCount(0);
        tip.setLikeCount(0);
        tip.setCollectCount(0);
        tip.setPublishTime(LocalDateTime.now());
        tip.setUpdateTime(LocalDateTime.now());
        healthTipMapper.insert(tip);
    }

    @Override
    public PageResult<TipListItemVO> pageTips(Long userId, TipPageQuery query) {
        int pageNum = query.page() <= 0 ? 1 : query.page();
        int pageSize = query.size() <= 0 ? 10 : query.size();

        Page<HealthTip> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HealthTip> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthTip::getStatus, 1);
        if (StringUtils.hasText(query.category())) {
            wrapper.eq(HealthTip::getCategory, query.category());
        }
        if (StringUtils.hasText(query.keyword())) {
            String keyword = query.keyword();
            wrapper.and(w -> w.like(HealthTip::getTitle, keyword)
                    .or()
                    .like(HealthTip::getSummary, keyword)
                    .or()
                    .like(HealthTip::getContent, keyword));
        }
        wrapper.orderByDesc(HealthTip::getPublishTime);
        IPage<HealthTip> result = healthTipMapper.selectPage(page, wrapper);

        List<HealthTip> records = result.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), Collections.emptyList());
        }

        Set<Long> userIds = records.stream().map(HealthTip::getUserId).collect(Collectors.toSet());
        List<User> authors = userIds.isEmpty() ? Collections.emptyList() : userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = authors.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        Map<Long, LikeRecord> likeMap = Collections.emptyMap();
        Map<Long, Collect> collectMap = Collections.emptyMap();
        if (userId != null) {
            List<Long> tipIds = records.stream().map(HealthTip::getId).toList();
            LambdaQueryWrapper<LikeRecord> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(LikeRecord::getUserId, userId)
                    .eq(LikeRecord::getTargetType, TARGET_TYPE_TIP)
                    .in(LikeRecord::getTargetId, tipIds);
            likeMap = likeRecordMapper.selectList(likeWrapper).stream()
                    .collect(Collectors.toMap(LikeRecord::getTargetId, Function.identity()));

            LambdaQueryWrapper<Collect> collectWrapper = new LambdaQueryWrapper<>();
            collectWrapper.eq(Collect::getUserId, userId)
                    .in(Collect::getTipId, tipIds);
            collectMap = collectMapper.selectList(collectWrapper).stream()
                    .collect(Collectors.toMap(Collect::getTipId, Function.identity()));
        }

        List<TipListItemVO> voList = new ArrayList<>(records.size());
        for (HealthTip record : records) {
            User author = userMap.get(record.getUserId());
            voList.add(TipListItemVO.builder()
                    .id(record.getId())
                    .title(record.getTitle())
                    .summary(record.getSummary())
                    .category(record.getCategory())
                    .tags(parseTags(record.getTags()))
                    .author(author != null ? author.getNickname() : "匿名用户")
                    .authorAvatar(author != null ? author.getAvatar() : null)
                    .publishTime(record.getPublishTime())
                    .viewCount(record.getViewCount())
                    .likeCount(record.getLikeCount())
                    .collectCount(record.getCollectCount())
                    .liked(likeMap.containsKey(record.getId()))
                    .collected(collectMap.containsKey(record.getId()))
                    .build());
        }

        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), voList);
    }

    @Override
    public TipDetailVO getTipDetail(Long userId, Long tipId) {
        HealthTip tip = healthTipMapper.selectById(tipId);
        if (tip == null || !Objects.equals(tip.getStatus(), 1)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "技巧不存在或已下架");
        }

        incrementViewCount(tipId);
        tip.setViewCount(tip.getViewCount() + 1);

        User author = userMapper.selectById(tip.getUserId());
        boolean liked = false;
        boolean collected = false;
        if (userId != null) {
            long likeCount = likeRecordMapper.selectCount(new QueryWrapper<LikeRecord>()
                    .lambda()
                    .eq(LikeRecord::getUserId, userId)
                    .eq(LikeRecord::getTargetType, TARGET_TYPE_TIP)
                    .eq(LikeRecord::getTargetId, tipId));
            liked = likeCount > 0;
            long collectCount = collectMapper.selectCount(new QueryWrapper<Collect>()
                    .lambda()
                    .eq(Collect::getUserId, userId)
                    .eq(Collect::getTipId, tipId));
            collected = collectCount > 0;
        }

        return TipDetailVO.builder()
                .id(tip.getId())
                .title(tip.getTitle())
                .content(tip.getContent())
                .summary(tip.getSummary())
                .category(tip.getCategory())
                .tags(parseTags(tip.getTags()))
                .authorId(tip.getUserId())
                .author(author != null ? author.getNickname() : "匿名用户")
                .authorAvatar(author != null ? author.getAvatar() : null)
                .publishTime(tip.getPublishTime())
                .viewCount(tip.getViewCount())
                .likeCount(tip.getLikeCount())
                .collectCount(tip.getCollectCount())
                .liked(liked)
                .collected(collected)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActionStateVO likeTip(Long userId, Long tipId) {
        HealthTip tip = ensureTipExists(tipId);
        LikeRecord record = likeRecordMapper.selectOne(new LambdaQueryWrapper<LikeRecord>()
                .eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getTargetType, TARGET_TYPE_TIP)
                .eq(LikeRecord::getTargetId, tipId)
                .last("LIMIT 1"));
        boolean active;
        if (record != null) {
            likeRecordMapper.deleteById(record.getId());
            decrementLikeCount(tipId);
            active = false;
        } else {
            LikeRecord like = new LikeRecord();
            like.setUserId(userId);
            like.setTargetType(TARGET_TYPE_TIP);
            like.setTargetId(tipId);
            like.setCreateTime(LocalDateTime.now());
            likeRecordMapper.insert(like);
            incrementLikeCount(tipId);
            active = true;
        }
        HealthTip latest = healthTipMapper.selectById(tip.getId());
        return ActionStateVO.builder()
                .action("like")
                .active(active)
                .likeCount(latest.getLikeCount())
                .collectCount(latest.getCollectCount())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActionStateVO collectTip(Long userId, Long tipId) {
        HealthTip tip = ensureTipExists(tipId);
        Collect record = collectMapper.selectOne(new LambdaQueryWrapper<Collect>()
                .eq(Collect::getUserId, userId)
                .eq(Collect::getTipId, tipId)
                .last("LIMIT 1"));
        boolean active;
        if (record != null) {
            collectMapper.deleteById(record.getId());
            decrementCollectCount(tipId);
            active = false;
        } else {
            Collect collect = new Collect();
            collect.setUserId(userId);
            collect.setTipId(tipId);
            collect.setCreateTime(LocalDateTime.now());
            collectMapper.insert(collect);
            incrementCollectCount(tipId);
            active = true;
        }
        HealthTip latest = healthTipMapper.selectById(tip.getId());
        return ActionStateVO.builder()
                .action("collect")
                .active(active)
                .likeCount(latest.getLikeCount())
                .collectCount(latest.getCollectCount())
                .build();
    }

    @Override
    public PageResult<TipListItemVO> pageUserTips(Long currentUserId, Long authorId, int page, int size) {
        int pageNum = Math.max(page, 1);
        int pageSize = Math.max(1, Math.min(size, 50));

        Page<HealthTip> pageRequest = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HealthTip> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthTip::getStatus, 1)
                .eq(HealthTip::getUserId, authorId)
                .orderByDesc(HealthTip::getPublishTime);
        IPage<HealthTip> result = healthTipMapper.selectPage(pageRequest, wrapper);

        List<HealthTip> records = result.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), Collections.emptyList());
        }

        User author = userMapper.selectById(authorId);

        List<Long> tipIds = records.stream().map(HealthTip::getId).toList();
        final Map<Long, LikeRecord> likeMap;
        final Map<Long, Collect> collectMap;
        if (currentUserId != null && !tipIds.isEmpty()) {
            LambdaQueryWrapper<LikeRecord> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(LikeRecord::getUserId, currentUserId)
                    .eq(LikeRecord::getTargetType, TARGET_TYPE_TIP)
                    .in(LikeRecord::getTargetId, tipIds);
            likeMap = likeRecordMapper.selectList(likeWrapper).stream()
                    .collect(Collectors.toMap(LikeRecord::getTargetId, Function.identity()));

            LambdaQueryWrapper<Collect> collectWrapper = new LambdaQueryWrapper<>();
            collectWrapper.eq(Collect::getUserId, currentUserId)
                    .in(Collect::getTipId, tipIds);
            collectMap = collectMapper.selectList(collectWrapper).stream()
                    .collect(Collectors.toMap(Collect::getTipId, Function.identity()));
        } else {
            likeMap = Collections.emptyMap();
            collectMap = Collections.emptyMap();
        }

        List<TipListItemVO> voList = records.stream().map(record -> TipListItemVO.builder()
                .id(record.getId())
                .title(record.getTitle())
                .summary(record.getSummary())
                .category(record.getCategory())
                .tags(parseTags(record.getTags()))
                .author(author != null ? author.getNickname() : "匿名用户")
                .authorAvatar(author != null ? author.getAvatar() : null)
                .publishTime(record.getPublishTime())
                .viewCount(record.getViewCount())
                .likeCount(record.getLikeCount())
                .collectCount(record.getCollectCount())
                .liked(likeMap.containsKey(record.getId()))
                .collected(collectMap.containsKey(record.getId()))
                .build()).toList();

        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), voList);
    }

    private HealthTip ensureTipExists(Long tipId) {
        HealthTip tip = healthTipMapper.selectById(tipId);
        if (tip == null || !Objects.equals(tip.getStatus(), 1)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "技巧不存在或已下架");
        }
        return tip;
    }

    private void incrementViewCount(Long tipId) {
        LambdaUpdateWrapper<HealthTip> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(HealthTip::getId, tipId)
                .setSql("view_count = view_count + 1");
        healthTipMapper.update(null, updateWrapper);
    }

    private void incrementLikeCount(Long tipId) {
        LambdaUpdateWrapper<HealthTip> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(HealthTip::getId, tipId)
                .setSql("like_count = like_count + 1");
        healthTipMapper.update(null, updateWrapper);
    }

    private void decrementLikeCount(Long tipId) {
        LambdaUpdateWrapper<HealthTip> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(HealthTip::getId, tipId)
                .setSql("like_count = CASE WHEN like_count > 0 THEN like_count - 1 ELSE 0 END");
        healthTipMapper.update(null, updateWrapper);
    }

    private void incrementCollectCount(Long tipId) {
        LambdaUpdateWrapper<HealthTip> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(HealthTip::getId, tipId)
                .setSql("collect_count = collect_count + 1");
        healthTipMapper.update(null, updateWrapper);
    }

    private void decrementCollectCount(Long tipId) {
        LambdaUpdateWrapper<HealthTip> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(HealthTip::getId, tipId)
                .setSql("collect_count = CASE WHEN collect_count > 0 THEN collect_count - 1 ELSE 0 END");
        healthTipMapper.update(null, updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTip(Long userId, Long tipId) {
        HealthTip tip = healthTipMapper.selectById(tipId);
        if (tip == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "技巧不存在");
        }
        
        // 权限校验：只能删除自己的技巧
        if (!tip.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权删除他人的技巧");
        }
        
        // 记录删除日志（在实际删除之前）
        User operator = userMapper.selectById(userId);
        DeleteLog deleteLog = DeleteLog.builder()
                .operatorId(userId)
                .operatorName(operator != null ? operator.getNickname() : "未知用户")
                .targetType("TIP")
                .targetId(tipId)
                .targetTitle(tip.getTitle())
                .deleteTime(LocalDateTime.now())
                .remark("软删除技巧，关联数据已级联删除")
                .build();
        deleteLogMapper.insert(deleteLog);
        
        // 软删除：设置status=0
        tip.setStatus(0);
        tip.setUpdateTime(LocalDateTime.now());
        healthTipMapper.updateById(tip);
        
        // 级联删除：删除该技巧的所有评论
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(Comment::getTipId, tipId);
        int deletedComments = commentMapper.delete(commentWrapper);
        
        // 级联删除：删除该技巧的所有收藏记录
        LambdaQueryWrapper<Collect> collectWrapper = new LambdaQueryWrapper<>();
        collectWrapper.eq(Collect::getTipId, tipId);
        int deletedCollects = collectMapper.delete(collectWrapper);
        
        // 级联删除：删除该技巧的所有点赞记录
        LambdaQueryWrapper<LikeRecord> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(LikeRecord::getTargetId, tipId)
                   .eq(LikeRecord::getTargetType, TARGET_TYPE_TIP);
        int deletedLikes = likeRecordMapper.delete(likeWrapper);
        
        // 更新删除日志的备注信息
        deleteLog.setRemark(String.format("软删除技巧，级联删除：%d条评论、%d条收藏、%d条点赞", 
                                          deletedComments, deletedCollects, deletedLikes));
        deleteLogMapper.updateById(deleteLog);
    }

    private String buildSummary(TipCreateRequest request) {
        if (StringUtils.hasText(request.summary())) {
            return request.summary();
        }
        String content = request.content();
        String plain = content.replaceAll("<[^>]*>", "");
        plain = plain.replaceAll("\\s+", " ").trim();
        if (plain.length() > 150) {
            plain = plain.substring(0, 150) + "...";
        }
        return plain;
    }

    private List<String> parseTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            return Collections.emptyList();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }
}
