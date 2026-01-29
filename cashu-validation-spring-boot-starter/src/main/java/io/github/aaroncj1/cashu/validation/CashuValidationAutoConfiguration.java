package io.github.aaroncj1.cashu.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.aaroncj1.cashu.core.serialization.TokenCodec;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@ConditionalOnClass({TokenCodec.class, WebMvcConfigurer.class})
@EnableConfigurationProperties(CashuValidationProperties.class)
public class CashuValidationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CashuTokenParser cashuTokenParser() {
        return new CashuTokenParser();
    }

    @Bean
    @ConditionalOnMissingBean
    public CashuMintClient cashuMintClient(RestClient.Builder restClientBuilder,
                                           CashuValidationProperties properties,
                                           ObjectMapper objectMapper) {
        return new CashuMintClient(restClientBuilder, properties, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public CashuTokenValidator cashuTokenValidator(CashuTokenParser parser,
                                                   CashuMintClient mintClient,
                                                   CashuValidationProperties properties) {
        return new CashuTokenValidator(parser, mintClient, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public CashuValidationInterceptor cashuValidationInterceptor(CashuTokenValidator validator,
                                                                 CashuValidationProperties properties,
                                                                 ObjectMapper objectMapper) {
        return new CashuValidationInterceptor(validator, properties, objectMapper);
    }

    @Bean
    public WebMvcConfigurer cashuValidationWebMvcConfigurer(CashuValidationInterceptor interceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(interceptor);
            }
        };
    }
}
