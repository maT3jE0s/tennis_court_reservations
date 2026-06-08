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

import com.example.demo.dto.CourtRequest;
import com.example.demo.entity.Court;
import com.example.demo.entity.SurfaceType;
import com.example.demo.repository.CourtRepository;
import com.example.demo.repository.SurfaceTypeRepository;

@ExtendWith(MockitoExtension.class)
public class CourtServiceTest {

    @Mock
    private SurfaceTypeRepository surfaceTypeRepository;

    @Mock
    private CourtRepository courtRepository;

    @InjectMocks
    private SurfaceTypeService surfaceTypeService;

    @InjectMocks
    private CourtService courtService;

    private SurfaceType surfaceType;
    private Court court;

    @BeforeEach
    void setUp() {
        surfaceType = new  SurfaceType("Grass", 0.5);
        surfaceType.setId(1L);
        court = new Court(1, surfaceType);
        court.setId(1L);
    }

    @Test
    void testCreate() {
        CourtRequest request = new CourtRequest(1, 1L);
        when(courtRepository.findByCourtNumber(1)).thenReturn(null);
        when(surfaceTypeRepository.findById(1L)).thenReturn(surfaceType);
        when(courtRepository.save(any(Court.class))).thenReturn(court);

        Court result = courtService.create(request);

        assertNotNull(result);
        assertEquals(1, result.getCourtNumber());
        verify(courtRepository).save(any(Court.class));
    }

    @Test
    void testCreate_duplicitNumber() {
        CourtRequest request = new CourtRequest(1, 1L);
        when(courtRepository.findByCourtNumber(1)).thenReturn(court);
        
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> courtService.create(request));

        assertEquals("Court with number 1 already exists", exception.getMessage());
    }

    @Test
    void testCreate_surfaceTypeNotFound() {
        CourtRequest request = new CourtRequest(1, 2L);
        when(courtRepository.findByCourtNumber(1)).thenReturn(null);
        when(surfaceTypeRepository.findById(2L)).thenReturn(null);
        
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> courtService.create(request));

        assertEquals("Surface type not found", exception.getMessage());
    }

    @Test
    void testGetAll() {
        when(courtRepository.findAll()).thenReturn(List.of(court));

        List<Court> result = courtService.getAll();

        assertEquals(1, result.size());
        assertEquals(result.get(0), court);
    }

    @Test
    void testGetById() {
        when(courtRepository.findById(1L)).thenReturn(court);

        Court result = courtService.getById(1L);

        assertNotNull(result);
        assertEquals(court, result);
    }

    @Test
    void testUpdate() {
        CourtRequest updateRequest = new CourtRequest(2, 1L);
        when(courtRepository.findById(1L)).thenReturn(court);
        when(surfaceTypeRepository.findById(1L)).thenReturn(surfaceType);
        when(courtRepository.findByCourtNumber(2)).thenReturn(null);
        when(courtRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Court result = courtService.update(1L, updateRequest);

        assertNotNull(result);
        assertEquals(2, result.getCourtNumber());
    }

    @Test
    void testUpdate_courtNotFound() {
        CourtRequest updateRequest = new CourtRequest(2, 1L);
        when(courtRepository.findById(2L)).thenReturn(null);
        
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> courtService.update(2L, updateRequest));

        assertEquals("Court not found", exception.getMessage());
    }

    @Test
    void testUpdate_surfaceTypeNotFound() {
        CourtRequest updateRequest = new CourtRequest(2, 2L);
        when(courtRepository.findById(1L)).thenReturn(court);
        when(surfaceTypeRepository.findById(2L)).thenReturn(null);
        
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> courtService.update(1L, updateRequest));

        assertEquals("Surface type not found", exception.getMessage());
    }

    @Test
    void testUpdate_duplicitCourtNumberButSameId() {
        CourtRequest updateRequest = new CourtRequest(2, 1L);
        when(courtRepository.findById(1L)).thenReturn(court);
        when(surfaceTypeRepository.findById(1L)).thenReturn(surfaceType);
        when(courtRepository.findByCourtNumber(2)).thenReturn(court);
        when(courtRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        
        Court result = courtService.update(1L, updateRequest);

        assertNotNull(result);
        assertEquals(2, result.getCourtNumber());
    }

    @Test
    void testUpdate_duplicitCourtNumber() {
        Court other = new Court(2, surfaceType);
        other.setId(2L);

        CourtRequest updateRequest = new CourtRequest(2, 1L);
        when(courtRepository.findById(1L)).thenReturn(court);
        when(surfaceTypeRepository.findById(1L)).thenReturn(surfaceType);
        when(courtRepository.findByCourtNumber(2)).thenReturn(other);
        
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> courtService.update(1L, updateRequest));

        assertEquals("Court with number 2 already exists", exception.getMessage());
    }

    @Test
    void testDelete() {
        when(courtRepository.findById(1L)).thenReturn(court);
        courtService.delete(1L);
        verify(courtRepository).delete(1L);
    }
}
