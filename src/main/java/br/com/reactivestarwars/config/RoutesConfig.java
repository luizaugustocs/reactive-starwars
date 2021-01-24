package br.com.reactivestarwars.config;

import br.com.reactivestarwars.handler.PlanetHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RoutesConfig {

    @Bean
    public RouterFunction<ServerResponse> planetRouter(PlanetHandler resource) {
        return RouterFunctions
                .route()
                .add(resource.buildRouter())
                .build();
    }
}
