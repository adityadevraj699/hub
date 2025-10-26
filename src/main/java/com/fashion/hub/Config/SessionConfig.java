package com.fashion.hub.Config;

import org.springframework.boot.web.servlet.server.Session;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SessionConfig {

    @Bean
    public org.springframework.boot.web.servlet.server.Session.Cookie sessionCookie() {
        org.springframework.boot.web.servlet.server.Session.Cookie cookie = new org.springframework.boot.web.servlet.server.Session.Cookie();
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // HTTPS me true
        return cookie;
    }
}
