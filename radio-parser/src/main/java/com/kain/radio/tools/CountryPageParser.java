package com.kain.radio.tools;

import com.kain.radio.model.Station;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;

@RequiredArgsConstructor
public class CountryPageParser {
    private final String countryPage;
    private static final String PAGINATION_BLOCK_ID = "#pagination-footer";
    public List<Station> parseAll() throws IOException {
        Document doc = Jsoup.connect(countryPage).get();
        Element pagination = doc.select(PAGINATION_BLOCK_ID).first();
        List<Station> stations = new ArrayList<>();
        if (pagination != null){ // get total pages
            for (Integer page : parsePageNumber(pagination)) {
                stations.addAll(new TablePageParser(countryPage,  page).parseTable());
            }
        } else {  // only one page - parse it
            stations.addAll(new TablePageParser(countryPage, 1).parseTable());
        }
        return stations;
    }

    private List<Integer> parsePageNumber(Element element){
        String[] pagesText = element.child(0).text().split(" ");
        TreeSet<Integer> bounds = new TreeSet<>();
        for (String text : pagesText){
            try {
                bounds.add(Integer.valueOf(text));
            }catch(NumberFormatException e){
                // ignore
            }
        }
        List<Integer> parsed = new ArrayList<>();
        for (int i = bounds.first(); i <= bounds.last(); i++){
            parsed.add(i);
        }
        return parsed;
    }
}
