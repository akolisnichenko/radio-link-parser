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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
        doc.select(COUNTRY_LINK_CLASS).forEach(this::parseCountryBlock);
    }

    private void parseCountryBlock(Element element) {
        if (element.children().size() > 0) {
            String countryLink = element.attr("href");
            String countryFlagSrc = element.child(0).attr("data-src");
            String countryCode = countryFlagSrc.substring(countryFlagSrc.length() - 6, countryFlagSrc.length() - 4);
            try {
                File countryFile = new File(countryCode +".list");
                FileWriter writer = new FileWriter(countryFile);
                List<Station> stations = new CountryPageParser(countryLink).parseAll();
                stations.stream()
                        .map(Station::toRow)
                        .forEach(str -> {
                                    try {
                                        writer.write(str + "\n");
                                    } catch (IOException e) {
                                        log.error("Couldn't write to file: {}", str, e);
                                    }
                                }

                        );
                writer.close();
                log.info("Total parsed for {}: {} ",countryCode, stations.size());
            } catch (IOException e) {
                log.error("Couldn't parse page for {} country: {}", countryLink, countryCode, e);
            }
        }
    }
}
