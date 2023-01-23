package com.kain.radio.model.js;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CipherData {
    private String cipher;
    private String iv;
    private String type;

    @JsonProperty("is_https")
    private boolean tlsSupport;

}
