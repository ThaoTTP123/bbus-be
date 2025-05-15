package com.fpt.bbusbe.model.enums;

public enum ErrCode {
    SUCCESS(0, "SUCCESS"),
    CAMERA_NOT_AVAILABLE(1, "The camera is not available"),
    FAILED_TO_GET_URI_IMAGE_DATA_CONTENT(466, "Failed to get URI image data content"),
    IMAGE_DATA_IS_TOO_LARGE(467, "Image data is too large"),
    FAILED_TO_EXTRACT_FACIAL_FEATURES(468, "Failed to extract facial features from pictures"),
    FAILED_TO_WRITE_IMAGE_DATA_IN_DATABASE(469, "Failed to write image data in database"),
    UNKNOWN(-1, "UNKNOWN");

    private final int code;
    private final String message;
    ErrCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
