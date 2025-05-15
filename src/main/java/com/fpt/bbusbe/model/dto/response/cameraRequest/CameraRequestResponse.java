package com.fpt.bbusbe.model.dto.response.cameraRequest;

import com.fpt.bbusbe.model.enums.CameraRequestStatus;
import com.fpt.bbusbe.model.enums.CameraRequestType;
import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CameraRequestResponse {
    private UUID cameraRequestId;
    private CameraRequestType requestType;
    private Date createdAt;
    private CameraRequestStatus status;
    private List<CameraRequestDetail> requests;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CameraRequestDetail{

        public CameraRequestDetail(com.fpt.bbusbe.model.entity.CameraRequestDetail cameraRequestDetail) {
            this.studentId = cameraRequestDetail.getId().getStudent().getId();
            this.name = cameraRequestDetail.getName();
            this.rollNumber = cameraRequestDetail.getRollNumber();
            this.personType = cameraRequestDetail.getPersonType() == 0 ? "ACTIVE" : "INACTIVE";
            this.avatar = cameraRequestDetail.getAvatar();
            this.errCode = getErrCode(cameraRequestDetail.getErrCode());
        }

        private UUID studentId;
        private String name;
        private String rollNumber;
        private String personType;
        private String avatar;
        private String errCode;

        private String getErrCode(int errCode) {
            return switch (errCode) {
                case 0 -> "Thêm ảnh thành công";
                case 1 -> "Camera khả dụng";
                case 461 -> "Đã tồn tại";
                case 466 -> "URL ảnh không hợp lệ";
                case 467 -> "Size ảnh quá lớn";
                case 468 -> "Không thể lấy mặt người từ ảnh";
                case 469 -> "Không thể lưu ảnh vào camera";
                default -> "Không biết";
            };
        }
    }


}

