package com.fpt.bbusbe.model.mqtt;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@JsonPropertyOrder({"messageId", "DataBegin", "operator", "PersonNum", "info", "DataEnd"})
public class PersonListMqttJson {
    private String messageId;
    @JsonProperty("DataBegin")
    private String dataBegin;
    private String operator;
    @JsonProperty("PersonNum")
    private String personNum;
    private List<PersonInfo> info;
    @JsonProperty("DataEnd")
    private String dataEnd;

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonInfo {
        private String customId;
        private String name;
        private int personType;
        private int tempCardType;
        private String cardValidBegin;
        private String cardValidEnd;
        private String picURI;
        // Getters and Setters
    }
}
