package com.application.manymilk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class YandexDiskConfig {

    @Value("${yandex.disk.token}")
    private String token;

    public String getToken() {
        return token;
    }
}
