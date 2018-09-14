package com.yukari.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;


@Configuration
@ConfigurationProperties(prefix = "myprops", ignoreUnknownFields = false)
@PropertySource("classpath:prop.properties")
@Data
@Component
public class PropProperties {

    private Integer roomId;
    private Integer bulletOpenplayMaxSize;
    private Integer bulletCloseplayMaxSize;
    private Integer uenterMaxSize;

}
