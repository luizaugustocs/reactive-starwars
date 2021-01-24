package br.com.reactivestarwars.handler;

import br.com.reactivestarwars.domain.Planet;
import br.com.reactivestarwars.repository.PlanetRemoteRepository;
import br.com.reactivestarwars.repository.PlanetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@Slf4j
public class PlanetHandler {

    private final PlanetRepository planetRepository;
    private final PlanetRemoteRepository planetRemoteRepository;


    public PlanetHandler(PlanetRepository planetRepository, PlanetRemoteRepository planetRemoteRepository) {
        this.planetRepository = planetRepository;
        this.planetRemoteRepository = planetRemoteRepository;
    }

    public RouterFunction<ServerResponse> buildRouter() {
        return RouterFunctions
                .route()
                .path("/planets", route -> route
                        .GET("/remote", this::getRemotePlanets)
                        .GET("/{id}", this::findById)
                        .DELETE("/{id}", this::delete))
                .GET(this::getAll)
                .POST(this::create)
                .build();

    }

    public Mono<ServerResponse> create(ServerRequest request) {

        return request.bodyToMono(Planet.class)
                .flatMap(this.planetRepository::save)
                .flatMap(planet -> ServerResponse.created(URI.create("/planets/" + planet.getId()))
                        .bodyValue(planet));
    }


    public Mono<ServerResponse> getAll(ServerRequest request) {

        String name = request.queryParam("name").orElse("");

        return ServerResponse.ok()
                .body(BodyInserters.fromPublisher(this.planetRepository.getAll(name), Planet.class));
    }

    public Mono<ServerResponse> findById(ServerRequest request) {

        String id = request.pathVariable("id");
        return this.planetRepository.findById(id)
                .flatMap(planet -> ServerResponse.ok().bodyValue(planet))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> getRemotePlanets(ServerRequest request) {

        Integer page = request.queryParam("page")
                .filter(param -> param.matches("^\\d+$"))
                .map(Integer::valueOf)
                .orElse(1);

        return ServerResponse.ok()
                .body(BodyInserters.fromPublisher(this.planetRemoteRepository.getPlanets(page), Planet.class));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {

        String id = request.pathVariable("id");

        return this.planetRepository.deleteById(id)
                .flatMap(planet -> ServerResponse.ok().bodyValue(planet))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

}
