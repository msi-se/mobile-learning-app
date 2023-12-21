package de.htwg_konstanz.mobilelearning.helper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MoodleUserIdResponse {
    public Integer userid;
    
    public MoodleUserIdResponse(@JsonProperty("userid") Integer userid) {   
        this.userid = userid;
    }

}
