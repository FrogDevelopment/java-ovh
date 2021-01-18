package com.ovh.api;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(OvhProperties.class)
public class OvhAutoConfiguration {

    @Bean
    OvhApi ovhApi(OvhProperties ovhProperties) {
        return new OvhApi(ovhProperties);
    }
}
