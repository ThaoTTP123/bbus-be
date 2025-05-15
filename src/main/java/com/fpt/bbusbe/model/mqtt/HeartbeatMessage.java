package com.fpt.bbusbe.model.mqtt;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeartbeatMessage {
    private String operator;
    private Info info;

    // Getters and setters

    @Getter
    @Setter
    public static class Info {
        private String facesluiceId;
        private String time;

        // Getters and setters
    }
}

