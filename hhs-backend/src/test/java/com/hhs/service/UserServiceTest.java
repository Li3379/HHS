package com.hhs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hhs.dto.LoginRequest;
import com.hhs.dto.RegisterRequest;
import com.hhs.entity.User;
import com.hhs.exception.BusinessException;
import com.hhs.mapper.UserMapper;
import com.hhs.security.JwtUtil;
import com.hhs.service.impl.UserServiceImpl;
import com.hhs.vo.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 用户服务单元测试
 * 
 * 测试目标：验证 UserService 的核心业务逻辑
 * 测试策略：使用 Mockito 模拟依赖，隔离测试
 * 
 * 学习要点：
 * 1. 如何使用 @Mock 和 @InjectMocks
 * 2. 如何 Mock MyBatis-Plus 的查询方法
 * 3. 如何验证方法调用
 * 
 * @author AI 测试导师
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务测试")
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        registerRequest = new RegisterRequest(
                "testuser",
                "password123",
                "Test User",
                "test@example.com"
        );

        loginRequest = new LoginRequest("testuser", "password123");

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setPassword("$2a$10$encodedPassword");
        mockUser.setNickname("Test User");
        mockUser.setEmail("test@example.com");
        mockUser.setStatus(1);
    }

    @Test
    @DisplayName("测试1.1：用户注册 - 成功场景")
    void testRegister_Success() {
        // Given: 用户名和邮箱都不存在
        // Mock MyBatis-Plus 的 selectOne() 方法返回 null（用户不存在）
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        
        // Mock selectCount() 方法返回 0（邮箱不存在）
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        
        // Mock 密码加密
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("$2a$10$encodedPassword");
        
        // Mock insert 方法
        when(userMapper.insert(any(User.class))).thenReturn(1);

        // When: 执行注册
        assertDoesNotThrow(() -> userService.register(registerRequest));

        // Then: 验证 insert 方法被调用了 1 次
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    @DisplayName("测试1.2：用户注册 - 用户名已存在")
    void testRegister_UsernameExists() {
        // Given: 用户名已存在
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockUser);

        // When & Then: 应该抛出业务异常
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.register(registerRequest));

        assertEquals("用户名已存在", exception.getMessage());
        
        // 验证 insert 方法从未被调用
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("测试1.3：用户注册 - 邮箱已存在")
    void testRegister_EmailExists() {
        // Given: 用户名不存在，但邮箱已存在
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);  // 邮箱已存在

        // When & Then: 应该抛出业务异常
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.register(registerRequest));

        assertEquals("邮箱已被注册", exception.getMessage());
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("测试2.1：用户登录 - 成功场景")
    void testLogin_Success() {
        // Given: 用户存在且密码正确
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockUser);
        when(passwordEncoder.matches(loginRequest.password(), mockUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(mockUser.getId(), mockUser.getUsername())).thenReturn("mock-jwt-token");

        // When: 执行登录
        AuthResponse response = userService.login(loginRequest);

        // Then: 验证返回结果
        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertNotNull(response.getUserInfo());
        assertEquals(mockUser.getId(), response.getUserInfo().getId());
        assertEquals(mockUser.getNickname(), response.getUserInfo().getNickname());
    }

    @Test
    @DisplayName("测试2.2：用户登录 - 用户不存在")
    void testLogin_UserNotFound() {
        // Given: 用户不存在
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When & Then: 应该抛出业务异常
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.login(loginRequest));

        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("测试2.3：用户登录 - 密码错误")
    void testLogin_WrongPassword() {
        // Given: 用户存在但密码错误
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockUser);
        when(passwordEncoder.matches(loginRequest.password(), mockUser.getPassword())).thenReturn(false);

        // When & Then: 应该抛出业务异常
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.login(loginRequest));

        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("测试2.4：用户登录 - 账号已禁用")
    void testLogin_AccountDisabled() {
        // Given: 账号已禁用
        mockUser.setStatus(0);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockUser);
        // 注意：不需要 Mock passwordEncoder.matches()，因为代码会先检查账号状态

        // When & Then: 应该抛出业务异常
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.login(loginRequest));

        assertEquals("用户名或密码错误", exception.getMessage());
        
        // 验证密码验证方法从未被调用（因为账号已禁用）
        verify(passwordEncoder, never()).matches(any(), any());
    }
}
