package com.kain.radio.tools;

import com.kain.radio.model.Station;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class TablePageParser{

    private final String initialTablePage;
    private final int page;

    private static final String PAGE_PARAM = "?page=";
    private static final String CELL_BLOCK_ID = ".mdc-grid-tile";

    public List<Station> parseTable() throws IOException {
        Document doc = Jsoup.connect(initialTablePage + PAGE_PARAM + page).get();
        Elements cells = doc.select(CELL_BLOCK_ID);
        return cells.stream()
                .map(this::parseCell)
                .filter(Objects::nonNull)
                .toList();
    }

    private Station parseCell(Element cell) {
        Element linkBlock = cell.firstElementChild();
        try {
            String link = Optional.ofNullable(linkBlock)
                .map(e -> e.attr("href"))
                        .orElseThrow();
            return  new RadioPageParser(initialTablePage + link).parseToStation();
        } catch (IOException e){
            log.error("Couldn't parse radio page", e);
            return null;
        }
    }


}
