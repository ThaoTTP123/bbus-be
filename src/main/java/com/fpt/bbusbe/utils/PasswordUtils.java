package com.fpt.bbusbe.utils;

import java.security.SecureRandom;
import java.util.regex.Pattern;

public class PasswordUtils {
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String ALL_CHARACTERS = UPPERCASE + LOWERCASE + DIGITS;
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 32;

    /**
     * Tạo một mật khẩu ngẫu nhiên thỏa mãn policy
     * @return mật khẩu ngẫu nhiên
     */
    public static String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
//        int length = random.nextInt(MAX_LENGTH - MIN_LENGTH + 1) + MIN_LENGTH; // Độ dài ngẫu nhiên từ 8-32
        int length = 8;

        StringBuilder password = new StringBuilder();

        // Đảm bảo có ít nhất một chữ hoa, một số
        password.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));

        // Thêm ký tự ngẫu nhiên khác
        for (int i = 2; i < length; i++) {
            password.append(ALL_CHARACTERS.charAt(random.nextInt(ALL_CHARACTERS.length())));
        }

        // Xáo trộn chuỗi
        return shuffleString(password.toString(), random);
    }

    /**
     * Kiểm tra xem mật khẩu có hợp lệ theo policy không
     * @param password mật khẩu cần kiểm tra
     * @return true nếu hợp lệ, ngược lại false
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            return false;
        }

        // Kiểm tra bằng regex
        String pattern = "^(?=.*[A-Z])(?=.*\\d).{" + MIN_LENGTH + "," + MAX_LENGTH + "}$";
        return Pattern.matches(pattern, password);
    }

    /**
     * Xáo trộn chuỗi để đảm bảo vị trí ngẫu nhiên
     */
    private static String shuffleString(String input, SecureRandom random) {
        char[] characters = input.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char temp = characters[i];
            characters[i] = characters[index];
            characters[index] = temp;
        }
        return new String(characters);
    }

    public static void main(String[] args) {
        // Tạo mật khẩu ngẫu nhiên
        String password = generateRandomPassword();
        System.out.println("Generated Password: " + password);

        // Kiểm tra tính hợp lệ của mật khẩu
        System.out.println("Is valid? " + isValidPassword(password));

        // Kiểm tra một số mật khẩu mẫu
        System.out.println("Check 'Abc12345': " + isValidPassword("Abc12345")); // Hợp lệ
        System.out.println("Check 'abcdefg': " + isValidPassword("abcdefg")); // Sai (thiếu số, chữ hoa)
        System.out.println("Check '12345678': " + isValidPassword("12345678")); // Sai (thiếu chữ hoa)
    }
}
