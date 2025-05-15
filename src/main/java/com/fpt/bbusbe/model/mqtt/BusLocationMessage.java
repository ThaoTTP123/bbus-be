package com.fpt.bbusbe.model.mqtt;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class BusLocationMessage {
    //{"gps":{"lat":21.014300,"lng":105.525520},"time":"2025-3-13 10:7:28"}
    private Gps gps;
    private String time;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Gps {
        private double lat;
        private double lng;
    }
}
