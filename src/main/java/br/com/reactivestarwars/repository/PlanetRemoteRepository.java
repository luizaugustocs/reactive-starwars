package br.com.reactivestarwars.repository;

import br.com.reactivestarwars.domain.Planet;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class PlanetRemoteRepository {

    private final WebClient remoteClient;

    public PlanetRemoteRepository(@Value("${swapi.baseUrl}") String baseUrl) {
        this.remoteClient = WebClient.create(baseUrl);
    }

    public Flux<Planet> getPlanets(Integer page) {
        String uri = UriComponentsBuilder.fromPath("/planets/")
                .queryParam("page", page)
                .build().toUriString();
        return this.remoteClient
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(ObjectNode.class)
                .flatMapMany(response -> {
                    ArrayNode results = (ArrayNode) response.get("results");

                    return Optional.ofNullable(results)
                            .filter(result -> !result.isEmpty())
                            .map(this::parseResult)
                            .map(Flux::fromStream)
                            .orElseGet(Flux::empty);

                });
    }

    private Stream<Planet> parseResult(ArrayNode result) {
        return StreamSupport
                .stream(result.spliterator(), false)
                .map(Planet::fromSWApi);
    }

    public Mono<Integer> getFilmCount(String planetName) {

        String uri = UriComponentsBuilder.fromPath("/planets/")
                .queryParam("search", planetName)
                .build().toUriString();

        return this.remoteClient
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(ObjectNode.class)
                .map(response -> {
                    ArrayNode results = (ArrayNode) response.get("results");
                    return Optional.ofNullable(results)
                            .filter(result -> !result.isEmpty())
                            .map(result -> extractFilmCount(result, planetName))
                            .orElse(0);
                });

    }

    private Integer extractFilmCount(ArrayNode results, String planetName) {
        return StreamSupport.stream(results.spliterator(), false)
                .filter(planet -> planet.get("name").asText("").equalsIgnoreCase(planetName))
                .findFirst()
                .flatMap(planet -> {
                    ArrayNode films = (ArrayNode) planet.get("films");
                    return Optional.ofNullable(films)
                            .map(ArrayNode::size);
                })
                .orElse(0);
    }
}
