package com.fpt.bbusbe.model.dto.response.student;

import com.fpt.bbusbe.model.enums.Gender;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentCameraResponse {
    private UUID studentId;
    private String studentName;
    private String rollNumber;
    private Gender gender;
    private String address;
    private String status;
    private String parentName;
    private String checkpointName;

    public static String setStatus(int status) {
        return switch (status) {
            case -1 -> "Chưa upload ảnh";
            case 0 -> "Thêm ảnh thành công";
            case 1 -> "Camera đang không khả dụng";
            case 461 -> "Đã tồn tại";
            case 466 -> "URL ảnh không hợp lệ";
            case 467 -> "Size ảnh quá lớn";
            case 468 -> "Không thể lấy mặt người từ ảnh";
            case 469 -> "Không thể lưu ảnh vào camera";
            default -> "Không biết";
        };
    }


}
