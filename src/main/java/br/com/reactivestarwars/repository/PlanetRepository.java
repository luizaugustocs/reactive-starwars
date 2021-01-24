package br.com.reactivestarwars.repository;

import br.com.reactivestarwars.RepositoryUtils;
import br.com.reactivestarwars.domain.Planet;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.EnhancedGlobalSecondaryIndex;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.Projection;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
public class PlanetRepository {

    private final DynamoDbAsyncTable<Planet> planetTable;
    private final PlanetRemoteRepository planetRemoteRepository;

    public PlanetRepository(DynamoDbEnhancedAsyncClient enhancedDbClient, DynamoDbAsyncClient dbClient,
                            PlanetRemoteRepository planetRemoteRepository) {

        this.planetTable = enhancedDbClient.table(Planet.class.getSimpleName(), TableSchema.fromBean(Planet.class));
        this.planetRemoteRepository = planetRemoteRepository;

        RepositoryUtils.createTableIfNotExists(dbClient, planetTable, (builder -> {
            EnhancedGlobalSecondaryIndex secondaryIndex = EnhancedGlobalSecondaryIndex.builder()
                    .indexName("name")
                    .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                    .build();
            builder.globalSecondaryIndices(secondaryIndex);

        }));
    }

    public Mono<Planet> save(Planet planetToSave) {
        String itemId = UUID.randomUUID().toString();
        planetToSave.setId(itemId);

        return this.planetRemoteRepository.getFilmCount(planetToSave.getName())
                .map(filmCount -> {
                    planetToSave.setFilmCount(filmCount);
                    return planetToSave;
                })
                .flatMap(this::saveAndGet);
    }

    private Mono<Planet> saveAndGet(Planet planet) {
        return Mono.fromFuture(this.planetTable.putItem(planet)
                .thenCompose(__ -> this.findById(planet.getId()).toFuture())
        );
    }

    public Flux<Planet> getAll(String name) {

        SdkPublisher<Planet> publisher = StringUtils.hasText(name)
                ? this.searchByName(name)
                : this.planetTable.scan().items();
        return Flux.from(publisher);
    }

    private SdkPublisher<Planet> searchByName(String name) {
        DynamoDbAsyncIndex<Planet> index = this.planetTable.index("name");
        SdkPublisher<Page<Planet>> indexQuery = index.query(builder -> {
            QueryConditional query = QueryConditional.keyEqualTo(Key.builder().partitionValue(name).build());
            builder.queryConditional(query);
        });
        return indexQuery.flatMapIterable(Page::items);
    }

    public Mono<Planet> findById(String id) {
        Key key = Key.builder().partitionValue(id).build();
        return Mono.fromFuture(this.planetTable.getItem(key));
    }

    public Mono<Planet> deleteById(String id) {
        Key key = Key.builder().partitionValue(id).build();
        return Mono.fromFuture(this.planetTable.deleteItem(key));
    }
}
