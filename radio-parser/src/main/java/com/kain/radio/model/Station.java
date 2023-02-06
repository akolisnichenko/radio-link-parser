package com.kain.radio.model;

import lombok.Data;

import java.net.URL;
import java.util.Set;

@Data
public class Station {
    private String externalId;
    private String name;
    private String country;
    private String description;
    private Set<String> categories;
    private Set<URL> links;
    private URL logo;
    private int rating; // 100 max

    public String toRow(){
        return  "{\""+name+"\", \""+ links.stream().findFirst().map(URL::toString).orElse("") + "\", \""+ logo.toString()+"\"},";
    }
}
