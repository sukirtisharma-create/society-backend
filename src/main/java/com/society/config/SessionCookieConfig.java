package com.society.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class SessionCookieConfig {

    @Bean
    public DefaultCookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();

        serializer.setCookieName("JSESSIONID");
        serializer.setSameSite("None");        // ✅ REQUIRED for cross-site
        serializer.setUseSecureCookie(true);   // ✅ REQUIRED for HTTPS (Railway)

        return serializer;
    }
}