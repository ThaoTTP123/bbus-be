package com.fpt.bbusbe.service.impl;

import com.fpt.bbusbe.exception.ResourceNotFoundException;
import com.fpt.bbusbe.model.dto.response.student.StudentResponse;
import com.fpt.bbusbe.model.entity.Student;
import com.fpt.bbusbe.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ParentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private ParentServiceImpl parentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindStudentsOfAParent_Success() {
        when(studentRepository.findByParent_User_Id(any())).thenReturn(Collections.emptyList());

        List<StudentResponse> responses = parentService.findStudentsOfAParent();

        assertNotNull(responses);
        verify(studentRepository, times(1)).findByParent_User_Id(any());
    }

    @Test
    void testFindStudentsOfAParent_NoStudents() {
        when(studentRepository.findByParent_User_Id(any())).thenReturn(Collections.emptyList());

        List<StudentResponse> responses = parentService.findStudentsOfAParent();

        assertNotNull(responses);
        verify(studentRepository, times(1)).findByParent_User_Id(any());
    }

    @Test
    void testRegisterCheckpoint_StudentNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> parentService.registerCheckpoint(null, null));
    }
}
