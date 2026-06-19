package com.gestion.incidents.chatservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class FeignAuthInterceptor implements RequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(FeignAuthInterceptor.class);

    @Override
    public void apply(RequestTemplate template) {
        // Get current HTTP request
        ServletRequestAttributes attributes =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // Forward the JWT token to incident-service
                template.header("Authorization", authHeader);
                log.debug("JWT token forwarded to incident-service");
            } else {
                log.warn("No Authorization header found in current request");
            }
        } else {
            log.warn("No request context available for Feign interceptor");
        }
    }
}
