package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dto.SurfaceTypeRequest;
import com.example.demo.entity.SurfaceType;
import com.example.demo.repository.SurfaceTypeRepository;

@ExtendWith(MockitoExtension.class)
public class SurfaceTypeServiceTest {
    @Mock
    private SurfaceTypeRepository surfaceTypeRepository;

    @InjectMocks
    private SurfaceTypeService surfaceTypeService;

    private SurfaceTypeRequest request;
    private SurfaceType surfaceType;

    @BeforeEach
    void setUp() {
        request = new SurfaceTypeRequest("Grass", 0.5);
        surfaceType = new  SurfaceType("Grass", 0.5);
        surfaceType.setId(1L);
    }

    @Test
    void testCreate() {
        when(surfaceTypeRepository.findByName("Grass")).thenReturn(null);
        when(surfaceTypeRepository.save(any(SurfaceType.class))).thenReturn(surfaceType);

        SurfaceType result = surfaceTypeService.create(request);

        assertNotNull(result);
        assertEquals("Grass", result.getName());
        assertEquals(0.5, result.getPricePerMinute());
        verify(surfaceTypeRepository).save(any(SurfaceType.class));
    }

    @Test
    void testCreate_duplicitName() {
        when(surfaceTypeRepository.findByName("Grass")).thenReturn(surfaceType);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> surfaceTypeService.create(request));

        assertEquals("Surface type Grass already exists", exception.getMessage());
    }

    @Test
    void testGetAll() {
        when(surfaceTypeRepository.findAll()).thenReturn(List.of(surfaceType));

        List<SurfaceType> result = surfaceTypeService.getAll();

        assertEquals(1, result.size());
        assertEquals(result.get(0), surfaceType);
    }

    @Test
    void testGetById() {
        when(surfaceTypeRepository.findById(1L)).thenReturn(surfaceType);

        SurfaceType result = surfaceTypeService.getById(1L);

        assertNotNull(result);
        assertEquals(result, surfaceType);
    }

    @Test
    void testUpdate() {
        SurfaceTypeRequest updateRequest = new SurfaceTypeRequest("Clay", 0.9);
        when(surfaceTypeRepository.findById(1L)).thenReturn(surfaceType);
        when(surfaceTypeRepository.findByName("Clay")).thenReturn(null);
        when(surfaceTypeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SurfaceType result = surfaceTypeService.update(1L, updateRequest);

        assertNotNull(result);
        assertEquals("Clay", result.getName());
        assertEquals(0.9, result.getPricePerMinute());
    }

    @Test
    void testUpdate_surfaceTypeNotFound() {
        SurfaceTypeRequest updateRequest = new SurfaceTypeRequest("Clay", 0.9);
        when(surfaceTypeRepository.findById(99L)).thenReturn(null);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> surfaceTypeService.update(99L, updateRequest));

        assertEquals("Surface type not found", exception.getMessage());
    }

    @Test
    void testUpdate_duplicitNameButSameId() {
        SurfaceTypeRequest updateRequest = new SurfaceTypeRequest("Grass", 0.9);
        when(surfaceTypeRepository.findById(1L)).thenReturn(surfaceType);
        when(surfaceTypeRepository.findByName("Grass")).thenReturn(surfaceType);
        when(surfaceTypeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SurfaceType result = surfaceTypeService.update(1L, updateRequest);

        assertNotNull(result);
        assertEquals("Grass", result.getName());
        assertEquals(0.9, result.getPricePerMinute());
    }

    @Test
    void testUpdate_duplicitName() {
        SurfaceType other = SurfaceType.builder().name("Clay").pricePerMinute(0.5).build();
        other.setId(2L);

        SurfaceTypeRequest updateRequest = new SurfaceTypeRequest("Clay", 0.9);
        when(surfaceTypeRepository.findById(1L)).thenReturn(surfaceType);
        when(surfaceTypeRepository.findByName("Clay")).thenReturn(other);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> surfaceTypeService.update(1L, updateRequest));

        assertEquals("Surface type Clay already exists", exception.getMessage());
    }

    @Test
    void testDelete() {
        when(surfaceTypeRepository.findById(1L)).thenReturn(surfaceType);
        surfaceTypeService.delete(1L);
        verify(surfaceTypeRepository).delete(1L);
    }
}
