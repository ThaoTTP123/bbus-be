package com.fpt.bbusbe.model.dto.request.student;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentUpdateAvatarRequest {
    private UUID id;
    private MultipartFile avatar;
}
