package com.fpt.bbusbe.service.impl;

import com.fpt.bbusbe.exception.ResourceNotFoundException;
import com.fpt.bbusbe.model.dto.response.student.StudentResponse;
import com.fpt.bbusbe.model.entity.Student;
import com.fpt.bbusbe.repository.StudentRepository;
import com.fpt.bbusbe.service.S3Service;
import com.fpt.bbusbe.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private UserService userService;

    @InjectMocks
    private StudentServiceImpl studentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindById_Success() {
        UUID studentId = UUID.randomUUID();
        Student mockStudent = new Student();
        mockStudent.setId(studentId);
        mockStudent.setAvatar("avatar.jpg");

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(mockStudent));
        when(s3Service.generatePresignedUrl(anyString())).thenReturn("http://example.com/avatar.jpg");

        StudentResponse response = studentService.findById(studentId);

        assertNotNull(response);
        assertEquals(studentId, response.getId());
        verify(studentRepository, times(1)).findById(studentId);
        verify(s3Service, times(1)).generatePresignedUrl(anyString());
    }

    @Test
    void testFindById_NotFound() {
        UUID studentId = UUID.randomUUID();

        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.findById(studentId));
    }

    @Test
    void testDelete_Success() {
        UUID studentId = UUID.randomUUID();
        Student mockStudent = new Student();
        mockStudent.setId(studentId);
        mockStudent.setAvatar("avatar.jpg");

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(mockStudent));

        studentService.delete(studentId);

        verify(studentRepository, times(1)).delete(mockStudent);
        verify(s3Service, times(1)).deleteFile(anyString());
    }
}
