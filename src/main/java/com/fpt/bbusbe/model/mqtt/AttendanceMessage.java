package com.fpt.bbusbe.model.mqtt;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendanceMessage {
    private String operator;
    private Info info;

    // Getters and setters

    @Getter
    @Setter
    public static class Info {
        private String customId;
        private String personId;
        @JsonProperty("RecordID")
        private String recordID;
        @JsonProperty("VerifyStatus")
        private String verifyStatus;
        @JsonProperty("PersonType")
        private String personType;
        @JsonProperty("similarity1")
        private float similarity1;
        @JsonProperty("similarity2")
        private float similarity2;
        @JsonProperty("Sendintime")
        private int sendInTime;
        private String direction;
        private String otype;
        @JsonProperty("persionName")
        private String personName;
        private String facesluiceId;
        private String facesluiceName;
        private String idCard;
        private String telnum;
        private int left;
        private int top;
        private int right;
        private int bottom;
        private String time;
        @JsonProperty("PushType")
        private String pushType;
        @JsonProperty("OpendoorWay")
        private String opendoorWay;
        private String cardNum2;
        @JsonProperty("RFIDCard")
        private String rfidCard;
        private String szQrCodeData;
        private String isNoMask;
        private String dwFileIndex;
        private String dwFilePos;
        private String pic;
    }
}