package com.hhs.common;

public interface ErrorCode {

    int SUCCESS = 0;
    int BAD_REQUEST = 400;
    int UNAUTHORIZED = 401;
    int FORBIDDEN = 403;
    int NOT_FOUND = 404;
    int CONFLICT = 409;
    int INTERNAL_ERROR = 500;
}
