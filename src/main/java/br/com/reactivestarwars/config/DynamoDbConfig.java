package br.com.reactivestarwars.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import java.net.URI;

@Configuration
public class DynamoDbConfig {

    private final String databaseEndpoint;
    private final String region;
    private final String accessKeyId;
    private final String secretKey;

    public DynamoDbConfig(@Value("${aws.dynamodb.endpoint}") String databaseEndpoint,
                          @Value("${aws.region:us-west-1}") String region,
                          @Value("${aws.accessKeyId}") String accessKeyId,
                          @Value("${aws.secretKey}") String secretKey) {
        this.databaseEndpoint = databaseEndpoint;
        this.region = region;
        this.accessKeyId = accessKeyId;
        this.secretKey = secretKey;
    }


    @Bean
    public DynamoDbAsyncClient dynamoDbAsyncClient() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(this.accessKeyId, this.secretKey);

        return DynamoDbAsyncClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(this.region))
                .endpointOverride(URI.create(this.databaseEndpoint))
                .build()
                ;
    }

    @Bean
    public DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient(DynamoDbAsyncClient dynamoDbAsyncClient) {
        return DynamoDbEnhancedAsyncClient.builder()
                .dynamoDbClient(dynamoDbAsyncClient)
                .build();
    }
}
