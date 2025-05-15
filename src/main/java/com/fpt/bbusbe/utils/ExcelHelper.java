package com.fpt.bbusbe.utils;

import com.fpt.bbusbe.exception.InvalidDataException;
import com.fpt.bbusbe.model.dto.request.student.StudentCreateNoImageRequest;
import com.fpt.bbusbe.model.dto.request.student.StudentCreationRequest;
import com.fpt.bbusbe.model.dto.request.user.UserCreationNoImageRequest;
import com.fpt.bbusbe.model.dto.response.excel.StudentImportResult;
import com.fpt.bbusbe.model.dto.response.excel.UserImportResult;
import com.fpt.bbusbe.model.entity.Parent;
import com.fpt.bbusbe.model.entity.User;
import com.fpt.bbusbe.model.enums.Role;
import com.fpt.bbusbe.model.dto.request.user.UserCreationRequest;
import com.fpt.bbusbe.model.enums.Gender;
import com.fpt.bbusbe.model.enums.StudentStatus;
import com.fpt.bbusbe.repository.ParentRepository;
import com.fpt.bbusbe.repository.StudentRepository;
import com.fpt.bbusbe.repository.UserRepository;
import com.fpt.bbusbe.service.S3Service;
import com.fpt.bbusbe.service.impl.S3ServiceImpl;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class ExcelHelper {

    private static ParentRepository parentRepository;
    private static UserRepository userRepository;
    private static StudentRepository studentRepository;
    private static S3Service s3Service;

    public ExcelHelper(ParentRepository parentRepository, UserRepository userRepository, StudentRepository studentRepository, S3Service s3Service) {
        ExcelHelper.parentRepository = parentRepository;
        ExcelHelper.userRepository = userRepository;
        ExcelHelper.studentRepository = studentRepository;
        ExcelHelper.s3Service = s3Service;
    }


    /**
     * Chuyển dữ liệu từ file Excel thành danh sách người dùng
     *
     * @param file
     * @param roleName
     * @return danh sách người dùng
     */
    public static UserImportResult excelToUsers(MultipartFile file, String roleName) {
        UserImportResult result = new UserImportResult();
        List<UserCreationNoImageRequest> users = new ArrayList<>();
        Map<Integer, String> errors = new HashMap<>();
        Set<String> phonesInFile = new HashSet<>();
        Set<String> emailsInFile = new HashSet<>();


        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            int lastRowIndex = sheet.getLastRowNum();

            int rowIndex = 0;

            if (rows.hasNext()) {
                rows.next(); // skip header
                rowIndex++;
            }

            // Tìm dòng cuối cùng thực sự có dữ liệu
            while (lastRowIndex > 0 && isRowEmpty(sheet.getRow(lastRowIndex))) {
                lastRowIndex--;
            }

            // Bắt đầu từ dòng 1 vì dòng 0 là tiêu đề
            for (int i = 1; i <= lastRowIndex; i++) {
                Row row = sheet.getRow(i);
                int excelRow = i + 1; // để hiển thị đúng số dòng như trong Excel
                StringBuilder rowError = new StringBuilder();

                if (row == null || isRowEmpty(row)) {
                    rowError.append("Dòng trống.");
                    errors.put(excelRow, "Lỗi dòng " + excelRow + ": " + rowError.toString().trim());
                    continue;
                }

                String s3Key;

                try {
                    String phone = getCellValue(row.getCell(0));
                    String name = getCellValue(row.getCell(1));
                    String genderStr = getCellValue(row.getCell(2));
                    String dobStr = getCellValue(row.getCell(3));
                    String email = getCellValue(row.getCell(4));
                    String avatarUrl = getCellValue(row.getCell(5));
                    String address = getCellValue(row.getCell(6));

                    s3Key = avatarUrl.substring(avatarUrl.lastIndexOf('/') + 1);

                    HttpURLConnection connection = null;
                    InputStream inputStream = null;
                    try {
                        // Open connection to the URL
                        URL url = new URL(avatarUrl);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.connect();

                        // Get input stream and content metadata
                        int contentLength = connection.getContentLength();
                        String contentType = connection.getContentType();
                        inputStream = connection.getInputStream();
                        // Upload to S3
                        s3Service.uploadFile( roleName.toLowerCase() + "s/" + s3Key, inputStream, contentLength, contentType);

                        System.out.println("Upload successful for: " + s3Key);
                    } catch (Exception e) {
                        System.err.println("Error during download or upload: " + e.getMessage());
                        // Ghi log lỗi
                        rowError.append("Lỗi tải lên ảnh đại diện, kiểm tra lại URL. ");
                        e.printStackTrace();
                    } finally {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Exception e) {
                                // ignore
                            }
                        }
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }

                    // Kiểm tra trùng sđt, email trong file
                    if (phonesInFile.contains(phone)) {
                        rowError.append("Số điện thoại '" + phone + "' đã tồn tại trong file. ");
                    } else {
                        phonesInFile.add(phone);
                    }
                    if (emailsInFile.contains(email)) {
                        rowError.append("Email '" + email + "' đã tồn tại trong file. ");
                    } else {
                        emailsInFile.add(email);
                    }

                    // Validate dữ liệu trống
                    if (phone.isEmpty()) rowError.append("Số điện thoại không được để trống. ");
                    if (name.isEmpty()) rowError.append("Tên không được để trống. ");
                    if (email.isEmpty()) rowError.append("Email không được để trống. ");
                    if (avatarUrl.isEmpty()) rowError.append("Avatar không được để trống. ");
                    if (address.isEmpty()) rowError.append("Địa chỉ không được để trống. ");
                    if (dobStr.isEmpty()) rowError.append("Ngày sinh không được để trống. ");

                    // Validate độ dài sđt
                    if (!phone.isEmpty() && phone.length() != 10) {
                        rowError.append("Số điện thoại phải có 10 chữ số. ");
                    }

                    // Validate format giới tính
                    Gender gender = null;
                    try {
                        gender = Gender.valueOf(genderStr.equals("Nam") ? "MALE" : "FEMALE");
                    } catch (Exception ex) {
                        rowError.append("Giới tính không hợp lệ. ");
                    }

                    // Validate ngày sinh
                    Date dob = null;
                    try {
                        dob = Date.valueOf(dobStr);
                    } catch (Exception ex) {
                        rowError.append("Định dạng ngày sinh không hợp lệ (yyyy-MM-dd). ");
                    }

                    // Kiểm tra trùng email, sđt
                    if (userRepository.findByEmail(email) != null) {
                        rowError.append("Email đã tồn tại. ");
                    }
                    if (userRepository.findByPhone(phone) != null) {
                        rowError.append("Số điện thoại đã tồn tại. ");
                    }

                    // Nếu có lỗi → ghi lỗi
                    if (rowError.length() > 0) {
                        errors.put(excelRow, "Lỗi dòng " + excelRow + ": " + rowError.toString().trim());
                    } else {
                        UserCreationNoImageRequest user = new UserCreationNoImageRequest();
                        String generated = PasswordUtils.generateRandomPassword();
                        user.setUsername(generated);
                        user.setPassword(generated);
                        user.setPhone(phone);
                        user.setName(name);
                        user.setGender(gender);
                        user.setDob(dob);
                        user.setEmail(email);
                        user.setAvatar(s3Key);
                        user.setAddress(address);
                        user.setRole(Role.valueOf(roleName));
                        users.add(user);
                    }

                } catch (Exception e) {
                    errors.put(excelRow, "Lỗi dòng " + excelRow + ": Lỗi không xác định - " + e.getMessage());
                }
            }

            workbook.close();
            result.setValidUsers(users);
            result.setErrorRows(errors);
            return result;

        } catch (Exception e) {
            throw new RuntimeException("Không thể đọc file Excel: " + e.getMessage());
        }
    }


    /**
     * Chuyển dữ liệu từ file Excel thành danh sách học sinh
     *
     * @param file
     * @return danh sách học sinh
     */
//    public static List<StudentCreationRequest> excelToStudents(MultipartFile file) {
//        try {
//            List<StudentCreationRequest> users = new ArrayList<>();
//            InputStream is = file.getInputStream();
//            Workbook workbook = WorkbookFactory.create(is);
//            Sheet sheet = workbook.getSheetAt(0);
//            Iterator<Row> rows = sheet.iterator();
//
//            // Bỏ qua dòng tiêu đề
//            if (rows.hasNext()) rows.next();
//
//            while (rows.hasNext()) {
//                Row currentRow = rows.next();
//
//                if (isRowEmpty(currentRow)) break;
//
//                StudentCreationRequest student = new StudentCreationRequest();
//                student.setRollNumber(currentRow.getCell(0).getStringCellValue());
//                student.setName(currentRow.getCell(1).getStringCellValue());
//                student.setGender(Gender.valueOf(getCellValue(currentRow.getCell(2)).equals("Nam") ? "MALE" : "FEMALE"));
//                student.setDob(Date.valueOf(getCellValue(currentRow.getCell(3))));
//                student.setAddress(currentRow.getCell(4).getStringCellValue());
//                student.setAvatar(currentRow.getCell(5).getStringCellValue());
//                student.setStatus(StudentStatus.valueOf("ACTIVE"));
//
//                // get phone of parent
//                String parentPhone = getCellValue(currentRow.getCell(6));
//                Parent parent = parentRepository.findParentIdByPhone(parentPhone).
//                        orElseThrow(() -> new RuntimeException("Parent with phone number: [" + parentPhone + "] not found!"));
//                student.setParentId(parent.getId());
//
//                users.add(student);
//            }
//            workbook.close();
//            return users;
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage());
//        }
//    }
    public static StudentImportResult excelToStudents(MultipartFile file) {
        StudentImportResult result = new StudentImportResult();
        List<StudentCreateNoImageRequest> students = new ArrayList<>();
        Map<Integer, String> errors = new HashMap<>();
        Set<String> rollNumbersInFile = new HashSet<>();

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);

            // Xác định dòng cuối thực sự
            int lastRowIndex = sheet.getLastRowNum();
            while (lastRowIndex > 0 && isRowEmpty(sheet.getRow(lastRowIndex))) {
                lastRowIndex--;
            }

            for (int i = 1; i <= lastRowIndex; i++) {
                Row row = sheet.getRow(i);
                int excelRow = i + 1;
                StringBuilder rowError = new StringBuilder();

                if (row == null || isRowEmpty(row)) {
                    errors.put(excelRow, "Lỗi dòng " + excelRow + ": Dòng trống.");
                    continue;
                }
                String s3Key;
                try {
                    String rollNumber = getCellValue(row.getCell(0));
                    String name = getCellValue(row.getCell(1));
                    String genderStr = getCellValue(row.getCell(2));
                    String dobStr = getCellValue(row.getCell(3));
                    String address = getCellValue(row.getCell(4));
                    String avatarUrl = getCellValue(row.getCell(5));
                    String parentPhone = getCellValue(row.getCell(6));
                    String className = getCellValue(row.getCell(7));

                    s3Key = avatarUrl.substring(avatarUrl.lastIndexOf('/') + 1);

                    HttpURLConnection connection = null;
                    InputStream inputStream = null;
                    try {
                        // Open connection to the URL
                        URL url = new URL(avatarUrl);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.connect();

                        // Get input stream and content metadata
                        int contentLength = connection.getContentLength();
                        String contentType = connection.getContentType();
                        inputStream = connection.getInputStream();
                        // Upload to S3
                        s3Service.uploadFile("students/" + s3Key, inputStream, contentLength, contentType);

                        System.out.println("Upload successful for: " + s3Key);
                    } catch (Exception e) {
                        System.err.println("Error during download or upload: " + e.getMessage());
                        rowError.append("Lỗi tải lên ảnh đại diện, kiểm tra lại URL. ");
                        e.printStackTrace();
                    } finally {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Exception e) {
                                // ignore
                            }
                        }
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }

                    // Validate trống
                    if (rollNumber.isEmpty()) rowError.append("Mã học sinh không được để trống. ");
                    if (name.isEmpty()) rowError.append("Tên không được để trống. ");
                    if (genderStr.isEmpty()) rowError.append("Giới tính không được để trống. ");
                    if (dobStr.isEmpty()) rowError.append("Ngày sinh không được để trống. ");
                    if (address.isEmpty()) rowError.append("Địa chỉ không được để trống. ");
                    if (avatarUrl.isEmpty()) rowError.append("Avatar không được để trống. ");
                    if (parentPhone.isEmpty()) rowError.append("Số điện thoại phụ huynh không được để trống. ");
                    if (className.isEmpty()) rowError.append("Tên lớp không được để trống. ");

                    // Check trùng rollNumber trong file
                    if (!rollNumber.isEmpty() && !rollNumbersInFile.add(rollNumber)) {
                        rowError.append("Mã học sinh '" + rollNumber + "' bị trùng trong file. ");
                    }

                    // Check rollNumber đã tồn tại trong DB
                    if (studentRepository.existsByRollNumber((rollNumber))) {
                        rowError.append("Mã học sinh '" + rollNumber + "' đã tồn tại trong hệ thống. ");
                    }

                    // Kiểm tra giới tính
                    Gender gender = null;
                    try {
                        gender = Gender.valueOf(genderStr.equals("Nam") ? "MALE" : "FEMALE");
                    } catch (Exception ex) {
                        rowError.append("Giới tính không hợp lệ. ");
                    }

                    // Kiểm tra ngày sinh
                    Date dob = null;
                    try {
                        dob = Date.valueOf(dobStr);
                    } catch (Exception ex) {
                        rowError.append("Định dạng ngày sinh không hợp lệ (yyyy-MM-dd). ");
                    }

                    // Kiểm tra tồn tại phụ huynh
                    Parent parent = null;
                    if (!parentPhone.isEmpty()) {
                        Optional<Parent> optionalParent = parentRepository.findParentIdByPhone(parentPhone);
                        if (optionalParent.isEmpty()) {
                            rowError.append("Không tìm thấy phụ huynh với số điện thoại '" + parentPhone + "'. ");
                        } else {
                            parent = optionalParent.get();
                        }
                    }

                    // Nếu có lỗi
                    if (rowError.length() > 0) {
                        errors.put(excelRow, "Lỗi dòng " + excelRow + ": " + rowError.toString().trim());
                    } else {
                        StudentCreateNoImageRequest student = new StudentCreateNoImageRequest();
                        student.setRollNumber(rollNumber);
                        student.setName(name);
                        student.setGender(gender);
                        student.setDob(dob);
                        student.setAddress(address);
                        student.setAvatar(s3Key);
                        student.setStatus(StudentStatus.ACTIVE);
                        student.setParentId(parent.getId());
                        student.setClassName(className);
                        students.add(student);
                    }

                } catch (Exception e) {
                    errors.put(excelRow, "Lỗi dòng " + excelRow + ": Lỗi không xác định - " + e.getMessage());
                }
            }

            workbook.close();
            result.setValidStudents(students);
            result.setErrorRows(errors);
            return result;

        } catch (Exception e) {
            throw new RuntimeException("Không thể đọc file Excel: " + e.getMessage());
        }
    }


    /**
     * Kiểm tra xem một dòng có trống không
     *
     * @param row
     * @return true nếu dòng trống, ngược lại false
     */
    private static boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    /**
     * Lấy giá trị của một ô trong Excel
     *
     * @param cell
     * @return giá trị của ô
     */
    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // Nếu là ngày tháng, định dạng về yyyy-MM-dd
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    return sdf.format(cell.getDateCellValue());
                } else {
                    // Nếu là số, chuyển thành chuỗi
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
            default:
                return "";
        }
    }
}
