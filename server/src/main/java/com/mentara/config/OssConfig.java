package com.mentara.config;

import com.obs.services.ObsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

@Configuration
public class OssConfig {

    // Prefer explicit configuration property, fall back to environment variable names.
    @Value("${huawei.obs.endpoint}")
    private String endpoint;

    @Value("${huawei.obs.access-key-id}")
    private String accessKeyProp;

    @Value("${huawei.obs.access-key-secret}")
    private String secretKeyProp;

    @Value("${huawei.obs.bucket-name}")
    private String bucketName;

    private final Environment env;

    public OssConfig(Environment env) {
        this.env = env;
    }

    private String resolveAccessKey() {
        // Order: explicit property (huawei.obs.access-key-id) -> env var HUAWEI_OBS_AK -> env var HUAWEI_OBS_ACCESS_KEY
        if (StringUtils.hasText(accessKeyProp)) return accessKeyProp;
        String fromEnv = env.getProperty("HUAWEI_OBS_AK");
        if (StringUtils.hasText(fromEnv)) return fromEnv;
        // alternative common name
        fromEnv = env.getProperty("HUAWEI_OBS_ACCESS_KEY");
        return StringUtils.hasText(fromEnv) ? fromEnv : null;
    }

    private String resolveSecretKey() {
        if (StringUtils.hasText(secretKeyProp)) return secretKeyProp;
        String fromEnv = env.getProperty("HUAWEI_OBS_SK");
        if (StringUtils.hasText(fromEnv)) return fromEnv;
        fromEnv = env.getProperty("HUAWEI_OBS_SECRET_KEY");
        return StringUtils.hasText(fromEnv) ? fromEnv : null;
    }

    @Bean
    public ObsClient obsClient() {
        String ak = resolveAccessKey();
        String sk = resolveSecretKey();

        if (!StringUtils.hasText(ak) || !StringUtils.hasText(sk) || !StringUtils.hasText(endpoint)) {
            // If credentials are missing, create a client with empty strings to avoid NPE during bean creation.
            // Real deployments should provide env vars or set properties in application.properties.
            return new ObsClient(ak == null ? "" : ak, sk == null ? "" : sk, endpoint == null ? "" : endpoint);
        }

        return new ObsClient(ak, sk, endpoint);
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getAccessKey() {
        return resolveAccessKey();
    }

    public String getSecretKey() {
        return resolveSecretKey();
    }

    public String getBucketName() {
        return bucketName;
    }
}