package com.kain.radio.tools;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kain.radio.model.Station;
import com.kain.radio.model.js.CipherData;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RadioPageParser {
    private final String initialLink;
    private static final String NAME_BLOCK = ".mdc-typography--display1";
    private static final String DESCRIPTION_BLOCK = ".radio-description";
    private static final String CATEGORIES_BLOCK = ".categories";
    private static final String RATING_ID = "#yellow_stars";
    private static final String IMAGE_ID = "#player_image";
    private static final String LAST_UPDATE_ID = "#last-update";
    private static final String KEY_ATTR = "data-timestamp";

    private final ObjectMapper mapper = new ObjectMapper();

    private static final String SCRIPT_BLOCK = "script";
    private static final String URL_ARRAY_BLOCK = "mytuner_vars.radio_playlists";

    public Station parseToStation() throws IOException {
        Document doc = Jsoup.connect(initialLink).get();
        String imgUrl = Optional.ofNullable(doc.select(IMAGE_ID).first())
                .map(element -> element.attr("src"))
                .orElse(null);
        String name = Optional.ofNullable(doc.select(NAME_BLOCK).first())
                .map(Element::text)
                .orElse("NO NAME");
    /*    int rating = Optional.ofNullable(doc.select(RATING_ID).first())
                .map(element -> element.attr("style"))
                .map(str -> str.substring(6,8))
                .map(Integer::valueOf)
                .orElse(0); */
        String description = Optional.ofNullable(doc.select(DESCRIPTION_BLOCK).first())
                .map(Element::text)
                .orElse("NO DESCRIPTION");
        Set<String> categories = Set.of(Optional.ofNullable(doc.select(CATEGORIES_BLOCK).first())
                .map(element -> element.childNode(2))
                .map(Node::outerHtml)
                .map(str -> str.split(","))
                .orElse(new String[0]));
        categories = categories.stream().map(String::trim).collect(Collectors.toSet());

        Optional<String> scriptBody = doc.select(SCRIPT_BLOCK).stream()
                .map(Element::html)
                .filter(str -> str.contains(URL_ARRAY_BLOCK))
                .findFirst();
        String timestamp = Optional.ofNullable(doc.select(LAST_UPDATE_ID).first())
                    .map(el -> el.attr(KEY_ATTR))
                .orElse("U");
        final String key = timestamp.repeat(32 / timestamp.length()).substring(0, 32);

        Set<URL> links = new HashSet<>();
        if (scriptBody.isPresent()){
            String allScript = scriptBody.get();
            int startIndex = allScript.indexOf(URL_ARRAY_BLOCK + "=") + URL_ARRAY_BLOCK.length() + 2;
            int endIndex = allScript.indexOf(";", startIndex) - 1;
            String jsonArray = allScript.substring(startIndex, endIndex).replace('\'','"' );
            List<CipherData> ciphers = mapper.readValue(jsonArray, new TypeReference<>() { });
            List<String> decodedLinks = ciphers.stream()
                    .map(data -> LinkDecoder.decodeLink(data, key))
                    .toList();
            for (String link : decodedLinks){
                links.add(new URL(link));
            }
        }

        Station station = new Station();
        station.setName(name);
        station.setDescription(description);
        station.setLogo(imgUrl != null ? new URL(imgUrl) : null);
        station.setRating(0);
        station.setCategories(categories);
        station.setLinks(links);

        return station;
    }
}
