package com.hhs.common;

public interface ErrorCode {

    int SUCCESS = 0;
    int BAD_REQUEST = 400;
    int UNAUTHORIZED = 401;
    int FORBIDDEN = 403;
    int NOT_FOUND = 404;
    int CONFLICT = 409;
    int INTERNAL_ERROR = 500;
    
    // AI相关错误码
    int AI_ERROR = 4001;           // AI服务异常
    int AI_PARSE_ERROR = 4002;     // AI响应解析失败
    int RATE_LIMIT_EXCEEDED = 429; // 超过限流
}
