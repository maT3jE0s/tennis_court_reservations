package com.example.demo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.example.demo.entity.Court;
import com.example.demo.entity.SurfaceType;

@DataJpaTest
@Import({CourtRepository.class, SurfaceTypeRepository.class})
public class CourtRepositoryTest {

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private SurfaceTypeRepository surfaceTypeRepository;

    private Court court;
    private SurfaceType surfaceType;

    @BeforeEach
    void setup() {
        surfaceType = surfaceTypeRepository.save(new SurfaceType("Grass", 0.5));
        court = courtRepository.save(new Court(1, surfaceType));
    }

    @Test
    void testUpdate() {
        court.setCourtNumber(7);
        Court result = courtRepository.save(court);

        assertNotNull(result);
        assertEquals(7, result.getCourtNumber());
    }
    
    @Test
    void testFindByCourtNumber() {
        Court result = courtRepository.findByCourtNumber(1);
        assertNotNull(result);
        assertEquals(court, result);
    }

    @Test
    void testFindByCourtNumber_notFound() {
        Court result = courtRepository.findByCourtNumber(2);
        assertNull(result);
    }
}
