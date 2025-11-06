package com.hhs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hhs.dto.TipCreateRequest;
import com.hhs.entity.Collect;
import com.hhs.entity.HealthTip;
import com.hhs.entity.LikeRecord;
import com.hhs.exception.BusinessException;
import com.hhs.mapper.CollectMapper;
import com.hhs.mapper.HealthTipMapper;
import com.hhs.mapper.LikeRecordMapper;
import com.hhs.mapper.UserMapper;
import com.hhs.service.impl.HealthTipServiceImpl;
import com.hhs.vo.ActionStateVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 健康技巧服务单元测试
 * 
 * 测试重点：
 * 1. 发布技巧功能
 * 2. 点赞功能的幂等性（重要！）
 * 3. 收藏功能的幂等性（重要！）
 * 4. 统计数据的准确性
 * 
 * 学习要点：
 * 1. 如何测试幂等性操作
 * 2. 如何 Mock 更新操作
 * 3. 如何验证数据库计数的增减
 * 
 * @author AI 测试导师
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("健康技巧服务测试")
class HealthTipServiceTest {

    @Mock
    private HealthTipMapper healthTipMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private LikeRecordMapper likeRecordMapper;

    @Mock
    private CollectMapper collectMapper;

    @InjectMocks
    private HealthTipServiceImpl healthTipService;

    private HealthTip mockTip;
    private TipCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        mockTip = new HealthTip();
        mockTip.setId(1L);
        mockTip.setUserId(1L);
        mockTip.setTitle("测试技巧");
        mockTip.setContent("测试内容");
        mockTip.setCategory("diet");
        mockTip.setTags("测试,健康");
        mockTip.setStatus(1);
        mockTip.setViewCount(0);
        mockTip.setLikeCount(0);
        mockTip.setCollectCount(0);

        createRequest = new TipCreateRequest(
                "每天喝8杯水",                                  // title
                "diet",                                           // category
                Arrays.asList("饮食", "健康"),                     // tags
                "保持充足的水分摄入对健康很重要...",             // content
                "每天定时喝水，保持身体水分平衡"                  // summary
        );
    }

    @Test
    @DisplayName("测试1.1：发布技巧 - 成功场景")
    void testCreateTip_Success() {
        // Given: Mock insert 返回成功
        when(healthTipMapper.insert(any(HealthTip.class))).thenReturn(1);

        // When: 发布技巧
        assertDoesNotThrow(() -> healthTipService.createTip(1L, createRequest));

        // Then: 验证 insert 被调用
        verify(healthTipMapper, times(1)).insert(any(HealthTip.class));
    }

    @Test
    @DisplayName("测试2.1：点赞功能 - 首次点赞（插入记录）")
    void testLikeTip_FirstTime_InsertRecord() {
        // Given: 技巧存在，用户未点赞
        when(healthTipMapper.selectById(1L)).thenReturn(mockTip);
        when(likeRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);  // 未点赞
        when(likeRecordMapper.insert(any(LikeRecord.class))).thenReturn(1);
        when(healthTipMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);

        // 模拟计数增加后的结果
        HealthTip updatedTip = new HealthTip();
        updatedTip.setId(1L);
        updatedTip.setLikeCount(1);  // 点赞后计数为 1
        updatedTip.setCollectCount(0);
        when(healthTipMapper.selectById(1L))
                .thenReturn(mockTip)           // 第一次：ensureTipExists
                .thenReturn(updatedTip);       // 第二次：获取最新计数

        // When: 执行点赞
        ActionStateVO result = healthTipService.likeTip(1L, 1L);

        // Then: 验证结果
        assertTrue(result.getActive(), "首次点赞应该返回 active=true");
        assertEquals(1, result.getLikeCount(), "点赞数应该为 1");
        assertEquals("like", result.getAction());

        // 验证插入点赞记录
        verify(likeRecordMapper, times(1)).insert(any(LikeRecord.class));
        
        // 验证点赞数增加（调用了 incrementLikeCount）
        verify(healthTipMapper, times(1)).update(isNull(), any(LambdaUpdateWrapper.class));
        
        // 验证没有删除操作
        verify(likeRecordMapper, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("测试2.2：点赞功能 - 再次点赞（取消点赞）")
    void testLikeTip_SecondTime_DeleteRecord() {
        // Given: 技巧存在，用户已点赞
        LikeRecord existingLike = new LikeRecord();
        existingLike.setId(100L);
        existingLike.setUserId(1L);
        existingLike.setTargetId(1L);
        existingLike.setTargetType(1);

        mockTip.setLikeCount(1);  // 已有1个赞

        when(healthTipMapper.selectById(1L)).thenReturn(mockTip);
        when(likeRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingLike);  // 已点赞
        when(likeRecordMapper.deleteById(100L)).thenReturn(1);
        when(healthTipMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);

        // 模拟取消点赞后的结果
        HealthTip updatedTip = new HealthTip();
        updatedTip.setId(1L);
        updatedTip.setLikeCount(0);  // 取消后计数为 0
        updatedTip.setCollectCount(0);
        when(healthTipMapper.selectById(1L))
                .thenReturn(mockTip)           // 第一次：ensureTipExists
                .thenReturn(updatedTip);       // 第二次：获取最新计数

        // When: 再次点赞（实际是取消）
        ActionStateVO result = healthTipService.likeTip(1L, 1L);

        // Then: 验证结果
        assertFalse(result.getActive(), "取消点赞应该返回 active=false");
        assertEquals(0, result.getLikeCount(), "点赞数应该变为 0");

        // 验证删除点赞记录
        verify(likeRecordMapper, times(1)).deleteById(100L);
        
        // 验证点赞数减少（调用了 decrementLikeCount）
        verify(healthTipMapper, times(1)).update(isNull(), any(LambdaUpdateWrapper.class));
        
        // 验证没有插入操作
        verify(likeRecordMapper, never()).insert(any(LikeRecord.class));
    }

    @Test
    @DisplayName("测试2.3：点赞功能 - 幂等性验证（连续3次点赞）")
    void testLikeTip_Idempotency() {
        // Given: 技巧存在
        when(healthTipMapper.selectById(1L)).thenReturn(mockTip);
        when(healthTipMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);

        // 第1次点赞：未点赞 -> 点赞
        when(likeRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(likeRecordMapper.insert(any(LikeRecord.class))).thenReturn(1);
        
        HealthTip tip1 = new HealthTip();
        tip1.setLikeCount(1);
        tip1.setCollectCount(0);
        when(healthTipMapper.selectById(1L))
                .thenReturn(mockTip)
                .thenReturn(tip1);

        // When: 第1次点赞
        ActionStateVO result1 = healthTipService.likeTip(1L, 1L);

        // Then: 验证第1次结果
        assertTrue(result1.getActive(), "第1次点赞应该成功");
        assertEquals(1, result1.getLikeCount());

        // 验证插入了1次，未删除
        verify(likeRecordMapper, times(1)).insert(any(LikeRecord.class));
        verify(likeRecordMapper, times(0)).deleteById(anyLong());
    }

    @Test
    @DisplayName("测试2.4：点赞功能 - 技巧不存在")
    void testLikeTip_TipNotFound() {
        // Given: 技巧不存在
        when(healthTipMapper.selectById(999L)).thenReturn(null);

        // When & Then: 应该抛出业务异常
        BusinessException exception = assertThrows(BusinessException.class,
                () -> healthTipService.likeTip(1L, 999L));

        assertEquals("技巧不存在或已下架", exception.getMessage());
        
        // 验证没有任何数据库操作
        verify(likeRecordMapper, never()).insert(any());
        verify(likeRecordMapper, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("测试3.1：收藏功能 - 首次收藏")
    void testCollectTip_FirstTime_InsertRecord() {
        // Given: 技巧存在，用户未收藏
        when(healthTipMapper.selectById(1L)).thenReturn(mockTip);
        when(collectMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);  // 未收藏
        when(collectMapper.insert(any(Collect.class))).thenReturn(1);
        when(healthTipMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);

        // 模拟收藏后的结果
        HealthTip updatedTip = new HealthTip();
        updatedTip.setId(1L);
        updatedTip.setLikeCount(0);
        updatedTip.setCollectCount(1);  // 收藏后计数为 1
        when(healthTipMapper.selectById(1L))
                .thenReturn(mockTip)
                .thenReturn(updatedTip);

        // When: 执行收藏
        ActionStateVO result = healthTipService.collectTip(1L, 1L);

        // Then: 验证结果
        assertTrue(result.getActive(), "首次收藏应该返回 active=true");
        assertEquals(1, result.getCollectCount(), "收藏数应该为 1");
        assertEquals("collect", result.getAction());

        // 验证插入收藏记录
        verify(collectMapper, times(1)).insert(any(Collect.class));
        
        // 验证收藏数增加
        verify(healthTipMapper, times(1)).update(isNull(), any(LambdaUpdateWrapper.class));
        
        // 验证没有删除操作
        verify(collectMapper, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("测试3.2：收藏功能 - 再次收藏（取消收藏）")
    void testCollectTip_SecondTime_DeleteRecord() {
        // Given: 技巧存在，用户已收藏
        Collect existingCollect = new Collect();
        existingCollect.setId(200L);
        existingCollect.setUserId(1L);
        existingCollect.setTipId(1L);

        mockTip.setCollectCount(1);  // 已有1个收藏

        when(healthTipMapper.selectById(1L)).thenReturn(mockTip);
        when(collectMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingCollect);  // 已收藏
        when(collectMapper.deleteById(200L)).thenReturn(1);
        when(healthTipMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);

        // 模拟取消收藏后的结果
        HealthTip updatedTip = new HealthTip();
        updatedTip.setId(1L);
        updatedTip.setLikeCount(0);
        updatedTip.setCollectCount(0);  // 取消后计数为 0
        when(healthTipMapper.selectById(1L))
                .thenReturn(mockTip)
                .thenReturn(updatedTip);

        // When: 再次收藏（实际是取消）
        ActionStateVO result = healthTipService.collectTip(1L, 1L);

        // Then: 验证结果
        assertFalse(result.getActive(), "取消收藏应该返回 active=false");
        assertEquals(0, result.getCollectCount(), "收藏数应该变为 0");

        // 验证删除收藏记录
        verify(collectMapper, times(1)).deleteById(200L);
        
        // 验证收藏数减少
        verify(healthTipMapper, times(1)).update(isNull(), any(LambdaUpdateWrapper.class));
        
        // 验证没有插入操作
        verify(collectMapper, never()).insert(any(Collect.class));
    }

    @Test
    @DisplayName("测试3.3：收藏功能 - 技巧不存在")
    void testCollectTip_TipNotFound() {
        // Given: 技巧不存在
        when(healthTipMapper.selectById(999L)).thenReturn(null);

        // When & Then: 应该抛出业务异常
        BusinessException exception = assertThrows(BusinessException.class,
                () -> healthTipService.collectTip(1L, 999L));

        assertEquals("技巧不存在或已下架", exception.getMessage());
        
        // 验证没有任何数据库操作
        verify(collectMapper, never()).insert(any());
        verify(collectMapper, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("测试4.1：点赞和收藏互不影响")
    void testLikeAndCollect_Independent() {
        // Given: 技巧存在
        when(healthTipMapper.selectById(1L)).thenReturn(mockTip);
        when(healthTipMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);

        // 点赞操作
        when(likeRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(likeRecordMapper.insert(any(LikeRecord.class))).thenReturn(1);
        
        HealthTip afterLike = new HealthTip();
        afterLike.setLikeCount(1);
        afterLike.setCollectCount(0);
        when(healthTipMapper.selectById(1L))
                .thenReturn(mockTip)
                .thenReturn(afterLike);

        // When: 执行点赞
        ActionStateVO likeResult = healthTipService.likeTip(1L, 1L);

        // Then: 验证点赞成功，收藏数不变
        assertTrue(likeResult.getActive());
        assertEquals(1, likeResult.getLikeCount());
        assertEquals(0, likeResult.getCollectCount(), "点赞不应该影响收藏数");

        // 收藏操作
        reset(healthTipMapper);  // 重置 Mock
        when(healthTipMapper.selectById(1L)).thenReturn(mockTip);
        when(collectMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(collectMapper.insert(any(Collect.class))).thenReturn(1);
        when(healthTipMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);

        HealthTip afterCollect = new HealthTip();
        afterCollect.setLikeCount(1);  // 点赞数保持
        afterCollect.setCollectCount(1);
        when(healthTipMapper.selectById(1L))
                .thenReturn(mockTip)
                .thenReturn(afterCollect);

        // When: 执行收藏
        ActionStateVO collectResult = healthTipService.collectTip(1L, 1L);

        // Then: 验证收藏成功，点赞数不变
        assertTrue(collectResult.getActive());
        assertEquals(1, collectResult.getLikeCount(), "收藏不应该影响点赞数");
        assertEquals(1, collectResult.getCollectCount());
    }
}

