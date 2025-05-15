package com.fpt.bbusbe.utils;

public class OtpEmailTemplateBuilder {

    public static String buildOtpEmail(String otpCode, String title, String footerNote) {
        return """
            <div style="font-family: sans-serif; padding: 24px; border: 1px solid #ddd; border-radius: 8px; max-width: 480px; margin: auto;">
                <h2 style="color: #2e86de; text-align: center;">🚍 %s</h2>
                <p>Xin chào,</p>
                <p>Mã OTP của bạn là:</p>
                <div style="font-size: 24px; font-weight: bold; background-color: #f1f1f1; padding: 12px 24px; border-radius: 6px; display: inline-block;">
                    %s
                </div>
                <p style="margin-top: 16px;">Mã OTP này có hiệu lực trong <strong>5 phút</strong>.</p>
                <hr style="margin: 24px 0;">
                <p style="font-size: 12px; color: gray;">%s</p>
            </div>
        """.formatted(title, otpCode, footerNote);
    }
}

