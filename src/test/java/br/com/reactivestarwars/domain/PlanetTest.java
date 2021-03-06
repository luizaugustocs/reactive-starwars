package br.com.reactivestarwars.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlanetTest {


    @Test
    public void convertFromSWApi() throws JsonProcessingException {

        ObjectNode node = new ObjectMapper().readValue("{\n" +
                "            \"name\": \"Tatooine\",\n" +
                "            \"terrain\": \"desert\",\n" +
                "            \"climate\": \"arid\"," +
                "            \"url\": \"http://swapi.dev/api/planets/1/\"\n" +
                "        }", ObjectNode.class);


        Planet result = Planet.fromSWApi(node);

        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("Tatooine", result.getName());
        assertEquals("desert", result.getTerrain());
        assertEquals("arid", result.getClimate());

    }
    @Test
    public void convertFromSWApi_withoutIdMatch() throws JsonProcessingException {

        ObjectNode node = new ObjectMapper().readValue("{\n" +
                "            \"name\": \"Tatooine\",\n" +
                "            \"terrain\": \"desert\",\n" +
                "            \"climate\": \"arid\"," +
                "            \"url\": \"http://swapi.dev/api/planets/\"\n" +
                "        }", ObjectNode.class);


        Planet result = Planet.fromSWApi(node);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals("Tatooine", result.getName());
        assertEquals("desert", result.getTerrain());
        assertEquals("arid", result.getClimate());
    }

}