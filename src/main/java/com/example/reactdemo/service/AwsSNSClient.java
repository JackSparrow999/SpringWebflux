package com.example.reactdemo.service;

import com.example.reactdemo.PrintJobNotificationModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import javax.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;

@Service
public class AwsSNSClient {

    private SnsAsyncClient snsAsyncClient;
    @Value("${amazonProperties.endpointUrl}")
    private String endpointUrl;
    @Value("${amazonProperties.bucketName}")
    private String bucketName;
    @Value("${amazonProperties.accessKey}")
    private String accessKey;
    @Value("${amazonProperties.secretKey}")
    private String secretKey;
    @Value("${amazonProperties.topicArn}")
    private String topicArn;


    @PostConstruct
    private void initializeAmazon() {

        AwsCredentialsProvider credentialsProvider = new AwsCredentialsProvider() {
            @Override
            public AwsCredentials resolveCredentials() {
                return AwsBasicCredentials.create(accessKey, secretKey);
            }
        };

        snsAsyncClient = SnsAsyncClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.US_EAST_1).build();


    }

    public void publishMessage(PrintJobNotificationModel obj) {
        String jsonString = new com.google.gson.Gson().toJson(obj);
        CompletableFuture<PublishResponse> response = snsAsyncClient.publish(p -> p.topicArn(topicArn).message(jsonString));
    }


}
