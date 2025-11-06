package com.hhs.common;

public interface Constants {

    String JWT_SECRET = "change-me-to-a-secure-secret";
    long JWT_EXPIRE_SECONDS = 7 * 24 * 60 * 60;

    String CACHE_USER_PREFIX = "hhs:user:";
}
