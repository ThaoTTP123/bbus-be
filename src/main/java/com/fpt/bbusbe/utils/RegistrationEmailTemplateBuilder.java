package com.fpt.bbusbe.utils;

public class RegistrationEmailTemplateBuilder {

    public static String buildRegistrationEmail(String fullName, String phone, String tempPassword, String role) {
        return """
            <div style="font-family: sans-serif; padding: 24px; border: 1px solid #ddd; border-radius: 8px; max-width: 520px; margin: auto;">
                <h2 style="color: #2e86de;">🎉 BBUS - Tài khoản đã được tạo</h2>
                <p>Xin chào <b>%s</b>,</p>
                <p>Bạn đã được tạo tài khoản để sử dụng hệ thống <strong>BBUS</strong>.</p>
                <p>Thông tin đăng nhập của bạn:</p>
                <ul>
                    <li><b>Số điện thoại đăng nhập:</b> %s</li>
                    <li><b>Mật khẩu tạm thời:</b> %s</li>
                    <li><b>Vai trò:</b> %s</li>
                </ul>
                <p style="margin-top: 16px;">📌 Vui lòng đăng nhập và <strong>đổi mật khẩu ngay</strong> để bảo mật tài khoản.</p>
                <hr style="margin: 24px 0;">
                <p style="font-size: 12px; color: gray;">Nếu bạn không yêu cầu tạo tài khoản BBUS, vui lòng liên hệ quản trị viên để được hỗ trợ.</p>
            </div>
        """.formatted(fullName, phone, tempPassword, role);
    }
}