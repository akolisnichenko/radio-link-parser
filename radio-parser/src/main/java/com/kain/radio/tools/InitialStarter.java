package com.kain.radio.tools;

import com.kain.radio.model.Station;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitialStarter implements ApplicationRunner {

    private static final String INITIAL_URL = "https://www.internetradio-horen.de";
    private static final String COUNTRY_LINK_CLASS = ".mdc-typography--caption";
    @Override
    public void run(ApplicationArguments args) throws Exception {
        //TODO: save radio list somewhere
        Document doc = Jsoup.connect(INITIAL_URL).get();
        List<Station> stations = doc.select(COUNTRY_LINK_CLASS)
                .stream()
                .map(this::parseCountryBlock)
                .flatMap(List::stream)
                .toList();
        log.info("Total parsed: {} ", stations.size());
    }

    private List<Station> parseCountryBlock(Element element) {
        if (element.children().size() > 0) {
            String countryLink = element.attr("href");
            String countryFlagSrc = element.child(0).attr("data-src");
            String countryCode = countryFlagSrc.substring(countryFlagSrc.length() - 6, countryFlagSrc.length() - 4);
            try {
                return new CountryPageParser(countryLink).parseAll();
            } catch (IOException e) {
                log.error("Couldn't parse page for {} country: {}", countryLink, countryCode, e);
            }
        }
        return Collections.emptyList();
    }
}
