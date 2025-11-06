package com.hhs.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    private String secret = "change-me";

    private long expireDays = 7;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpireDays() {
        return expireDays;
    }

    public void setExpireDays(long expireDays) {
        this.expireDays = expireDays;
    }
}
