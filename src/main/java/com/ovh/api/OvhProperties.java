package com.ovh.api;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotBlank;

@Data
@ConfigurationProperties("ovh.api")
public class OvhProperties {

    @NotBlank
    private String endpoint;
    @NotBlank
    private String applicationKey;
    @NotBlank
    private String applicationSecret;
    @NotBlank
    private String consumerKey;

    private int readTimeOut = 30000;
    private int connectTimeOut = 30000;
}
