package de.htwg_konstanz.mobilelearning.helper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
class MoodleTokenResponse {
    public String token;

    public MoodleTokenResponse(
        @JsonProperty("token") String token
    ) {
        this.token = token;
    }
}