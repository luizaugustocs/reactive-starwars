package br.com.reactivestarwars.repository;

import br.com.reactivestarwars.domain.Planet;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
public class PlanetRemoteRepositoryTest {

    private PlanetRemoteRepository planetRemoteRepository;

    private ObjectMapper mapper = new ObjectMapper();

    public static MockWebServer mockedApi;

    @BeforeAll
    public static void setUpMock() throws IOException {
        mockedApi = new MockWebServer();
        mockedApi.start();
    }

    @AfterAll
    public static void tearDownMock() throws IOException {
        mockedApi.shutdown();
    }

    @BeforeEach
    public void setUp() {
        this.planetRemoteRepository = new PlanetRemoteRepository("http://localhost:" + mockedApi.getPort());
    }

    @Test
    public void getFilmCount() throws IOException, InterruptedException {

        String value = mapper.readValue(getClass().getResourceAsStream("/mockPlanetResponse.json"), JsonNode.class).toString();
        mockedApi.enqueue(new MockResponse()
                .setBody(value)
                .addHeader("Content-Type", "application/json"));

        Mono<Integer> result = this.planetRemoteRepository.getFilmCount("Tatooine");

        StepVerifier.create(result)
                .expectNext(5)
                .verifyComplete();

        RecordedRequest recordedRequest = mockedApi.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/planets/?search=Tatooine", recordedRequest.getPath());
    }
    @Test
    public void getFilmCount_notExactMatch() throws IOException, InterruptedException {

        String value = mapper.readValue(getClass().getResourceAsStream("/mockPlanetResponse.json"), JsonNode.class).toString();
        mockedApi.enqueue(new MockResponse()
                .setBody(value)
                .addHeader("Content-Type", "application/json"));

        Mono<Integer> result = this.planetRemoteRepository.getFilmCount("Tatoo");

        StepVerifier.create(result)
                .expectNext(0)
                .verifyComplete();

        RecordedRequest recordedRequest = mockedApi.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/planets/?search=Tatoo", recordedRequest.getPath());
    }


    @Test
    public void getFilmCount_emptyArray() throws IOException, InterruptedException {

        String value = mapper.readValue(getClass().getResourceAsStream("/mockPlanetResponse_emptyFilms.json"), JsonNode.class).toString();
        mockedApi.enqueue(new MockResponse()
                .setBody(value)
                .addHeader("Content-Type", "application/json"));

        Mono<Integer> result = this.planetRemoteRepository.getFilmCount("Tatooine");

        StepVerifier.create(result)
                .expectNext(0)
                .verifyComplete();

        RecordedRequest recordedRequest = mockedApi.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/planets/?search=Tatooine", recordedRequest.getPath());
    }
    @Test
    public void getFilmCount_missingFilms() throws IOException, InterruptedException {

        String value = mapper.readValue(getClass().getResourceAsStream("/mockPlanetResponse_missingFilms.json"), JsonNode.class).toString();
        mockedApi.enqueue(new MockResponse()
                .setBody(value)
                .addHeader("Content-Type", "application/json"));

        Mono<Integer> result = this.planetRemoteRepository.getFilmCount("Tatooine");

        StepVerifier.create(result)
                .expectNext(0)
                .verifyComplete();

        RecordedRequest recordedRequest = mockedApi.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/planets/?search=Tatooine", recordedRequest.getPath());
    }
    @Test
    public void getFilmCount_emptyResults() throws IOException, InterruptedException {

        String value = mapper.readValue(getClass().getResourceAsStream("/mockPlanetResponse_emptyResults.json"), JsonNode.class).toString();
        mockedApi.enqueue(new MockResponse()
                .setBody(value)
                .addHeader("Content-Type", "application/json"));

        Mono<Integer> result = this.planetRemoteRepository.getFilmCount("Tatooine");

        StepVerifier.create(result)
                .expectNext(0)
                .verifyComplete();

        RecordedRequest recordedRequest = mockedApi.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/planets/?search=Tatooine", recordedRequest.getPath());
    }

    @Test
    public void getPlanets() throws InterruptedException, IOException {
        String value = mapper.readValue(getClass().getResourceAsStream("/mockManyPlanetResponse.json"), JsonNode.class).toString();
        mockedApi.enqueue(new MockResponse()
                .setBody(value)
                .addHeader("Content-Type", "application/json"));

        Flux<Planet> result = this.planetRemoteRepository.getPlanets(1);

        StepVerifier.create(result)
                .expectNextMatches(planet -> {
                    return planet.getId().equals("1") &&
                            planet.getName().equals("Tatooine") &&
                            planet.getClimate().equals("arid") &&
                            planet.getTerrain().equals("desert") &&
                            planet.getFilmCount().equals(5);
                }).expectNextMatches(planet -> {
                    return  planet.getId().equals("2") &&
                            planet.getName().equals("Alderaan") &&
                            planet.getClimate().equals("temperate") &&
                            planet.getTerrain().equals("grasslands, mountains") &&
                            planet.getFilmCount().equals(2);
                })
                .verifyComplete();

        RecordedRequest recordedRequest = mockedApi.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/planets/?page=1", recordedRequest.getPath());

    }

}