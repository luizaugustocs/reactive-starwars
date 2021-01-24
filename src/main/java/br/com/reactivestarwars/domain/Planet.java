package br.com.reactivestarwars.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Data;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Data
@DynamoDbBean
public class Planet {

    @Getter(onMethod_ = {@DynamoDbPartitionKey})
    private String id;

    @Getter(onMethod_ = {@DynamoDbSecondaryPartitionKey(indexNames = {"name"})})
    private String name;

    @Getter(onMethod_ = {@DynamoDbAttribute("terrain")})
    private String terrain;

    @Getter(onMethod_ = {@DynamoDbAttribute("climate")})
    private String climate;

    @Getter(onMethod_ = {@DynamoDbAttribute("filmCount")})
    private Integer filmCount;


    public static Planet fromSWApi(JsonNode planetObject) {
        Planet planet = new Planet();

        planet.setId(extractIdFromUrl(planetObject));
        planet.setName(planetObject.get("name").asText());
        planet.setClimate(planetObject.get("climate").asText());
        planet.setTerrain(planetObject.get("terrain").asText());

        ArrayNode films = (ArrayNode) planetObject.get("films");
        Integer filmCount = Optional.ofNullable(films)
                .map(ArrayNode::size)
                .orElse(0);
        planet.setFilmCount(filmCount);
        return planet;
    }

    private static String extractIdFromUrl(JsonNode planetObject) {
        String url = planetObject.get("url").asText();

        Pattern regex = Pattern.compile("\\d+");

        Matcher match = regex.matcher(url);

        return match.find() ? match.group(0) : null;
    }


}
