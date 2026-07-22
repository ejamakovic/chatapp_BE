package com.evolt.chatapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Only avatars are public. Message attachments and post images go
        // through their own secured controllers (/attachments/{id},
        // /posts/{id}/image) which check membership/visibility first.
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations("file:uploads/avatars/");
        registry.addResourceHandler("/uploads/groups/**")
                .addResourceLocations("file:uploads/groups/");
    }
}