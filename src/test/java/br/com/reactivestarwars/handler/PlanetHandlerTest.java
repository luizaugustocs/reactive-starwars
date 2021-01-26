package br.com.reactivestarwars.handler;

import br.com.reactivestarwars.LocalDynamoExtension;
import br.com.reactivestarwars.domain.Planet;
import br.com.reactivestarwars.repository.PlanetRemoteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.comparator.Comparators;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

@ExtendWith(LocalDynamoExtension.class)
@SpringBootTest
@AutoConfigureWebTestClient
public class PlanetHandlerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private DynamoDbEnhancedAsyncClient enhancedDbClient;

    private DynamoDbAsyncTable<Planet> table;

    @MockBean
    private PlanetRemoteRepository planetRemoteRepository;

    @BeforeEach
    public void setUp() {
        this.table = enhancedDbClient.table(Planet.class.getSimpleName(), TableSchema.fromBean(Planet.class));

    }

    @AfterEach
    public void tearDown() {


        this.table.scan()
                .items()
                .subscribe(planet -> table.deleteItem(planet).join())
                .join();
    }

    @Test
    public void create() {

        Planet planet = new Planet();
        planet.setName("Earth");
        planet.setClimate("temperate");
        planet.setTerrain("grasslands, mountains");

        Mockito.when(this.planetRemoteRepository.getFilmCount("Earth")).thenReturn(Mono.just(0));

        EntityExchangeResult<Planet> result = webTestClient.post()
                .uri("/planets")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(planet)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Planet.class)
                .returnResult();


        assertNotNull(result);
        assertNotNull(result.getResponseBody());
        assertNotNull(result.getResponseBody().getId());
        assertEquals("Earth", result.getResponseBody().getName());
        assertEquals("temperate", result.getResponseBody().getClimate());
        assertEquals("grasslands, mountains", result.getResponseBody().getTerrain());
        assertEquals(0, result.getResponseBody().getFilmCount());

        verify(this.planetRemoteRepository, only()).getFilmCount("Earth");
    }

    @Test
    public void create_WithFilms() {

        Planet planet = new Planet();
        planet.setName("Tatooine");
        planet.setClimate("arid");
        planet.setTerrain("desert");


        Mockito.when(this.planetRemoteRepository.getFilmCount("Tatooine")).thenReturn(Mono.just(5));

        EntityExchangeResult<Planet> result = webTestClient.post()
                .uri("/planets")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(planet)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Planet.class)
                .returnResult();


        assertNotNull(result);
        assertNotNull(result.getResponseBody());
        assertNotNull(result.getResponseBody().getId());
        assertEquals("Tatooine", result.getResponseBody().getName());
        assertEquals("arid", result.getResponseBody().getClimate());
        assertEquals("desert", result.getResponseBody().getTerrain());
        assertEquals(5, result.getResponseBody().getFilmCount());

        verify(this.planetRemoteRepository, only()).getFilmCount("Tatooine");
    }


    @Test
    public void findAll_notFound() {


        WebTestClient.ListBodySpec<Planet> result = webTestClient.get()
                .uri("/planets")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Planet.class);

        assertTrue(result.returnResult().getResponseBody().isEmpty());

    }
    @Test
    public void findAll() {

        Planet planet = new Planet();
        planet.setId(UUID.randomUUID().toString());
        planet.setName("Alderaan");
        planet.setTerrain("grasslands, mountains");
        planet.setClimate("temperate");
        planet.setFilmCount(2);

        Planet planet2 = new Planet();
        planet2.setId(UUID.randomUUID().toString());
        planet2.setName("Tatooine");
        planet2.setClimate("arid");
        planet2.setTerrain("desert");
        planet2.setFilmCount(5);

        this.table.putItem(planet).join();
        this.table.putItem(planet2).join();


        EntityExchangeResult<List<Planet>> result = webTestClient.get()
                .uri("/planets")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Planet.class)
                .returnResult();

        assertNotNull(result.getResponseBody());
        assertEquals(2, result.getResponseBody().size());

        List<Planet> orderedPlanets = result.getResponseBody();

        orderedPlanets.sort(Comparator.comparing(Planet::getName));

        assertEquals("Alderaan", orderedPlanets.get(0).getName());
        assertEquals("grasslands, mountains", orderedPlanets.get(0).getTerrain());
        assertEquals("temperate", orderedPlanets.get(0).getClimate());
        assertEquals(2, orderedPlanets.get(0).getFilmCount());

        assertEquals("Tatooine", orderedPlanets.get(1).getName());
        assertEquals("arid", orderedPlanets.get(1).getClimate());
        assertEquals("desert", orderedPlanets.get(1).getTerrain());
        assertEquals(5, orderedPlanets.get(1).getFilmCount());


    }
    @Test
    public void searchByName() {


        Planet planet = new Planet();
        planet.setId(UUID.randomUUID().toString());
        planet.setName("Alderaan");
        planet.setTerrain("grasslands, mountains");
        planet.setClimate("temperate");
        planet.setFilmCount(2);

        Planet planet2 = new Planet();
        planet2.setId(UUID.randomUUID().toString());
        planet2.setName("Tatooine");
        planet2.setClimate("arid");
        planet2.setTerrain("desert");
        planet2.setFilmCount(5);

        this.table.putItem(planet).join();
        this.table.putItem(planet2).join();


        EntityExchangeResult<List<Planet>> result = webTestClient.get()
                .uri("/planets?name=Tatooine")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Planet.class)
                .returnResult();

        assertNotNull(result.getResponseBody());
        assertEquals(1, result.getResponseBody().size());

        assertEquals("Tatooine", result.getResponseBody().get(0).getName());
        assertEquals("arid", result.getResponseBody().get(0).getClimate());
        assertEquals("desert", result.getResponseBody().get(0).getTerrain());
        assertEquals(5, result.getResponseBody().get(0).getFilmCount());

    }

    @Test
    public void findOne_notFound() {
        webTestClient.get()
                .uri("/planets/123")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().isEmpty();
    }

    @Test
    public void findOne() {

        String id = UUID.randomUUID().toString();
        Planet planet = new Planet();
        planet.setId(id);
        planet.setName("Mars");
        planet.setTerrain("desert");
        planet.setClimate("dry");

        this.table.putItem(planet).join();
        EntityExchangeResult<Planet> result = webTestClient.get()
                .uri("/planets/" + id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Planet.class)
                .returnResult();

        assertNotNull(result);
        assertNotNull(result.getResponseBody());
        assertEquals(id, result.getResponseBody().getId());
        assertEquals("Mars", result.getResponseBody().getName());
        assertEquals("dry", result.getResponseBody().getClimate());
        assertEquals("desert", result.getResponseBody().getTerrain());
    }

    @Test
    public void delete_notFound() {
        webTestClient.delete()
                .uri("/planets/123")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().isEmpty();
    }

    @Test
    public void delete() {
        String id = UUID.randomUUID().toString();
        Planet planet = new Planet();
        planet.setId(id);
        planet.setName("Alderaan");
        planet.setTerrain("grasslands, mountains");
        planet.setClimate("temperate");
        planet.setFilmCount(2);

        this.table.putItem(planet).join();
        EntityExchangeResult<Planet> result = webTestClient.get()
                .uri("/planets/" + id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Planet.class)
                .returnResult();

        assertNotNull(result);
        assertNotNull(result.getResponseBody());
        assertEquals(id, result.getResponseBody().getId());
        assertEquals("Alderaan", result.getResponseBody().getName());
        assertEquals("grasslands, mountains", result.getResponseBody().getTerrain());
        assertEquals("temperate", result.getResponseBody().getClimate());
        assertEquals(2, result.getResponseBody().getFilmCount());
    }

    @Test
    public void getRemote_withoutPage() {


        Mockito.when(this.planetRemoteRepository.getPlanets(1)).thenReturn(this.createPlanets());

        EntityExchangeResult<List<Planet>> result = webTestClient.get()
                .uri("/planets/remote")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Planet.class)
                .returnResult();

        assertNotNull(result.getResponseBody());
        assertEquals(2, result.getResponseBody().size());

        verify(this.planetRemoteRepository, only()).getPlanets(1);
    }
    @Test
    public void getRemote_withPage() {

        Mockito.when(this.planetRemoteRepository.getPlanets(3)).thenReturn(this.createPlanets());

        EntityExchangeResult<List<Planet>> result = webTestClient.get()
                .uri("/planets/remote?page=3")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Planet.class)
                .returnResult();

        assertNotNull(result.getResponseBody());
        assertEquals(2, result.getResponseBody().size());

        verify(this.planetRemoteRepository, only()).getPlanets(3);
    }
    @Test
    public void getRemote_withNonNumericPage() {

        Mockito.when(this.planetRemoteRepository.getPlanets(1)).thenReturn(this.createPlanets());

        EntityExchangeResult<List<Planet>> result = webTestClient.get()
                .uri("/planets/remote?page=1a")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Planet.class)
                .returnResult();

        assertNotNull(result.getResponseBody());
        assertEquals(2, result.getResponseBody().size());

        verify(this.planetRemoteRepository, only()).getPlanets(1);
    }

    private Flux<Planet> createPlanets() {
        Planet planet = new Planet();
        planet.setId(UUID.randomUUID().toString());
        planet.setName("Alderaan");
        planet.setTerrain("grasslands, mountains");
        planet.setClimate("temperate");
        planet.setFilmCount(2);

        Planet planet2 = new Planet();
        planet2.setId(UUID.randomUUID().toString());
        planet2.setName("Tatooine");
        planet2.setClimate("arid");
        planet2.setTerrain("desert");
        planet2.setFilmCount(5);

        return Flux.just(planet, planet2);
    }

}