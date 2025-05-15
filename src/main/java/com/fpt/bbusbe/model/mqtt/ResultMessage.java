package com.fpt.bbusbe.model.mqtt;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResultMessage {
    //mqtt/face/1001001/Ack {
    //"messageId":"AddPersonsList20250405121917558",
    //"operator": "AddPersons-Ack",
    //"code":"200",
    //"info": {
    //"facesluiceId":"1001001",
    //"AddErrNum":"1",
    //"AddSucNum":"0",
    //"AddErrInfo":[{"customId":"cc099cf3-6edd-4225-82d7-38472020967e","errcode":"466"}],
    //"AddSucInfo":[],
    //"result":"ok"
    //}
    //}

    private String messageId;
    private String operator;
    private int code;
    private Info info;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Info {
        private int facesluiceId;
        @JsonProperty("AddErrNum")
        private int addErrNum;
        @JsonProperty("AddSucNum")
        private int addSucNum;
        @JsonProperty("AddErrInfo")
        private List<AddErrInfo> addErrInfos;
        @JsonProperty("AddSucInfo")
        private List<AddSucInfo> addSucInfos;
        private String result;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AddErrInfo {
        private String customId;
        @JsonProperty("errcode")
        private int errCode;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AddSucInfo {
        private String customId;
    }
}
