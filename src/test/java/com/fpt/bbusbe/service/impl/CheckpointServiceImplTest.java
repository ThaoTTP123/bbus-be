package com.fpt.bbusbe.service.impl;

import com.fpt.bbusbe.exception.ResourceNotFoundException;
import com.fpt.bbusbe.model.dto.request.checkpoint.CheckpointCreationRequest;
import com.fpt.bbusbe.model.dto.request.checkpoint.CheckpointStatusChangeRequest;
import com.fpt.bbusbe.model.dto.request.checkpoint.CheckpointUpdateRequest;
import com.fpt.bbusbe.model.dto.response.checkpoint.CheckpointPageResponse;
import com.fpt.bbusbe.model.dto.response.checkpoint.CheckpointResponse;
import com.fpt.bbusbe.model.entity.Checkpoint;
import com.fpt.bbusbe.model.enums.CheckpointStatus;
import com.fpt.bbusbe.repository.CheckpointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CheckpointServiceImplTest {

    @Mock
    private CheckpointRepository checkpointRepository;

    @InjectMocks
    private CheckpointServiceImpl checkpointService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll() {
        String keyword = "test";
        String sort = "name:asc";
        int page = 1;
        int size = 10;

        Page<Checkpoint> mockPage = new PageImpl<>(Collections.emptyList());
        when(checkpointRepository.searchByKeyword(anyString(), any(Pageable.class))).thenReturn(mockPage);

        CheckpointPageResponse response = checkpointService.findAll(keyword, sort, page, size);

        assertNotNull(response);
        assertEquals(0, response.getCheckpoints().size());
        verify(checkpointRepository, times(1)).searchByKeyword(anyString(), any(Pageable.class));
    }

    @Test
    void testFindById() {
        UUID id = UUID.randomUUID();
        Checkpoint checkpoint = Checkpoint.builder()
                .name("Test Checkpoint")
                .description("Description")
                .latitude(String.valueOf(10.0))
                .longitude(String.valueOf(20.0))
                .status(CheckpointStatus.ACTIVE)
                .build();
        checkpoint.setId(id);

        when(checkpointRepository.findById(id)).thenReturn(Optional.of(checkpoint));

        CheckpointResponse response = checkpointService.findById(id);

        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals("Test Checkpoint", response.getName());
        verify(checkpointRepository, times(1)).findById(id);
    }

    @Test
    void testSave() {
        CheckpointCreationRequest request = new CheckpointCreationRequest();
        request.setCheckpointName("New Checkpoint");
        request.setDescription("Description");
        //Random longitude and latitude
        request.setLatitude(String.valueOf(10.0));
        request.setLongitude(String.valueOf(20.0));

        Checkpoint checkpoint = Checkpoint.builder()
                .name(request.getCheckpointName())
                .description(request.getDescription())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .status(CheckpointStatus.ACTIVE)
                .build();
        checkpoint.setId(UUID.randomUUID());
        when(checkpointRepository.findByName(request.getCheckpointName())).thenReturn(null);
        when(checkpointRepository.save(any(Checkpoint.class))).thenReturn(checkpoint);

        CheckpointResponse response = checkpointService.save(request);

        assertNotNull(response);
        assertEquals(request.getCheckpointName(), response.getName());
        verify(checkpointRepository, times(1)).findByName(request.getCheckpointName());
        verify(checkpointRepository, times(1)).save(any(Checkpoint.class));
    }

    @Test
    void testUpdate() {
        UUID id = UUID.randomUUID();
        CheckpointUpdateRequest request = new CheckpointUpdateRequest();
        request.setId(id);
        request.setCheckpointName("Updated Checkpoint");
        request.setDescription("Updated Description");
        request.setLatitude(String.valueOf(15.0));
        request.setLongitude(String.valueOf(25.0));

        Checkpoint checkpoint = Checkpoint.builder()
                .name("Old Checkpoint")
                .description("Old Description")
                .latitude(String.valueOf(10.0))
                .longitude(String.valueOf(20.0))
                .status(CheckpointStatus.ACTIVE)
                .build();
        checkpoint.setId(id);

        when(checkpointRepository.findById(id)).thenReturn(Optional.of(checkpoint));
        when(checkpointRepository.save(any(Checkpoint.class))).thenReturn(checkpoint);

        CheckpointResponse response = checkpointService.update(request);

        assertNotNull(response);
        assertEquals(request.getCheckpointName(), response.getName());
        verify(checkpointRepository, times(1)).findById(id);
        verify(checkpointRepository, times(1)).save(any(Checkpoint.class));
    }

    @Test
    void testChangeStatus() {
        UUID id = UUID.randomUUID();
        CheckpointStatusChangeRequest request = new CheckpointStatusChangeRequest();
        request.setId(id);
        request.setStatus(CheckpointStatus.INACTIVE);

        Checkpoint checkpoint = Checkpoint.builder()
                .status(CheckpointStatus.ACTIVE)
                .build();
        checkpoint.setId(id);

        when(checkpointRepository.findById(id)).thenReturn(Optional.of(checkpoint));

        checkpointService.changeStatus(request);

        assertEquals(CheckpointStatus.INACTIVE, checkpoint.getStatus());
        verify(checkpointRepository, times(1)).findById(id);
        verify(checkpointRepository, times(1)).save(checkpoint);
    }

    @Test
    void testDelete() {
        UUID id = UUID.randomUUID();

        Checkpoint checkpoint = Checkpoint.builder()
                .build();

        checkpoint.setId(id);
        when(checkpointRepository.findById(id)).thenReturn(Optional.of(checkpoint));

        checkpointService.delete(id);

        verify(checkpointRepository, times(1)).findById(id);
        verify(checkpointRepository, times(1)).deleteById(id);
    }

    @Test
    void testFindById_NotFound() {
        UUID id = UUID.randomUUID();
        when(checkpointRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> checkpointService.findById(id));
        verify(checkpointRepository, times(1)).findById(id);
    }
}
