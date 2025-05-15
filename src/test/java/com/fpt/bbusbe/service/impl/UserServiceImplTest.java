package com.fpt.bbusbe.service.impl;

import com.fpt.bbusbe.exception.ResourceNotFoundException;
import com.fpt.bbusbe.model.dto.response.user.UserResponse;
import com.fpt.bbusbe.model.entity.User;
import com.fpt.bbusbe.repository.UserRepository;
import com.fpt.bbusbe.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindById_Success() {
        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setAvatar("avatar.jpg");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(s3Service.generatePresignedUrl(anyString())).thenReturn("http://example.com/avatar.jpg");

        UserResponse response = userService.findById(userId);

        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        verify(userRepository, times(1)).findById(userId);
        verify(s3Service, times(1)).generatePresignedUrl(anyString());
    }

    @Test
    void testFindById_NotFound() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.findById(userId));
    }

    @Test
    void testDelete_Success() {
        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setAvatar("avatar.jpg");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        userService.delete(userId);

        verify(userRepository, times(1)).deleteById(userId);
        verify(s3Service, times(1)).deleteFile(anyString());
    }
}
