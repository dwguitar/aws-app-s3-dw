package com.dw.awsapps3dw.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws")
public record AwsProperties(
        String region,
        S3Properties s3
) {
    public record S3Properties(String bucketName, String defaultPrefix) {

        public String bucketArn() {
            return "arn:aws:s3:::" + bucketName;
        }
    }
}
