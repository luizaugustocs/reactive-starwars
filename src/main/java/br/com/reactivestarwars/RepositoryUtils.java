package br.com.reactivestarwars;

import br.com.reactivestarwars.domain.Planet;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.model.CreateTableEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.EnhancedGlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

public final class RepositoryUtils {


    public static void createTableIfNotExists(DynamoDbAsyncClient dbClient, DynamoDbAsyncTable<?> table,  Consumer<CreateTableEnhancedRequest.Builder> buildRequestConsumer ) {
        dbClient.describeTable(DescribeTableRequest.builder()
                .tableName(table.tableName())
                .build())
                .exceptionally(ex -> {
                    if (ex.getCause() instanceof ResourceNotFoundException) {
                        table.createTable(buildRequestConsumer).join();
                        return null;
                    }
                    throw new CompletionException(ex);
                }).join();
    }
}
