package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dto.ReservationRequest;
import com.example.demo.entity.Court;
import com.example.demo.entity.Reservation;
import com.example.demo.entity.SurfaceType;
import com.example.demo.entity.User;
import com.example.demo.entity.Reservation.GameType;
import com.example.demo.repository.CourtRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private CourtRepository courtRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Reservation reservation;
    private Court court;
    private SurfaceType surfaceType;
    private User user;
    private ReservationRequest request;

    private final LocalDateTime START = LocalDateTime.of(2026, 1, 1, 10, 0);
    private final LocalDateTime END   = LocalDateTime.of(2026, 1, 1, 11, 0);

    @BeforeEach
    void setUp() {
        surfaceType = new  SurfaceType("Grass", 0.5);
        surfaceType.setId(1L);

        court = new Court(1, surfaceType);
        court.setId(1L);

        user = new User("0900123456", "John");
        user.setId(1L);

        reservation = Reservation.builder()
                .court(court)
                .user(user)
                .startTime(START)
                .endTime(END)
                .gameType(GameType.SINGLE)
                .totalPrice(30)
                .build();
        reservation.setId(1L);

        request = new ReservationRequest(
            1,
            "0900123456",
            "John",
            java.time.LocalDateTime.of(2025, 1, 1, 10, 0),
            java.time.LocalDateTime.of(2025, 1, 1, 11, 0),
            Reservation.GameType.SINGLE);

    }

    @Test
    void testCreate_singles() {
        when(courtRepository.findByCourtNumber(1)).thenReturn(court);
        when(reservationRepository.overlaps(any(), any(), any(), any())).thenReturn(false);
        when(userRepository.findByPhoneNumber("0900123456")).thenReturn(user);
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Reservation result = reservationService.create(request);

        assertNotNull(result);
        assertEquals(court, result.getCourt());
        assertEquals(user, result.getUser());
        assertEquals(30.0, result.getTotalPrice());
    }

    @Test
    void testCreate_doubles() {
        request.setGameType(GameType.DOUBLE);
        when(courtRepository.findByCourtNumber(1)).thenReturn(court);
        when(reservationRepository.overlaps(any(), any(), any(), any())).thenReturn(false);
        when(userRepository.findByPhoneNumber("0900123456")).thenReturn(user);
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Reservation result = reservationService.create(request);

        assertNotNull(result);
        assertEquals(court, result.getCourt());
        assertEquals(user, result.getUser());
        assertEquals(45.0, result.getTotalPrice());

    }

    @Test
    void testCreate_newUser() {
        when(courtRepository.findByCourtNumber(1)).thenReturn(court);
        when(reservationRepository.overlaps(any(), any(), any(), any())).thenReturn(false);
        when(userRepository.findByPhoneNumber("0900123456")).thenReturn(null);
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        reservationService.create(request);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreate_invalidTime() {
        when(courtRepository.findByCourtNumber(1)).thenReturn(court);
        request.setStartTime(END);
        request.setStartTime(START);

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> reservationService.create(request));

        assertEquals("Reservation must start before it ends", ex.getMessage());
    }

    @Test
    void testCreate_courtNotFound() {
        when(courtRepository.findByCourtNumber(1)).thenReturn(null);
        
        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> reservationService.create(request));

        assertEquals("Court not found", ex.getMessage());
    }

    @Test
    void testCreate_overlap() {
        when(courtRepository.findByCourtNumber(1)).thenReturn(court);
        when(reservationRepository.overlaps(any(), any(), any(), any())).thenReturn(true);
        
        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> reservationService.create(request));

        assertEquals("The time interval overlaps with an existing reservation", ex.getMessage());
    }

    @Test
    void testGetByCourtNumber() {
        when(reservationRepository.findByCourtNumber(1)).thenReturn(List.of(reservation));

        List<Reservation> result = reservationService.getByCourtNumber(1);

        assertEquals(1, result.size());
        assertEquals(result.get(0), reservation);
    }

    @Test
    void testGetById() {
        when(reservationRepository.findById(1L)).thenReturn(reservation);

        Reservation result = reservationService.getById(1L);

        assertNotNull(result);
        assertEquals(reservation, result);
    }

    @Test
    void testGetByPhoneNumber() {
        when(reservationRepository.findByPhoneNumber("0900123456",
            true)).thenReturn(List.of(reservation));

        List<Reservation> result = reservationService.getByPhoneNumber("0900123456",
            true);

        assertEquals(1, result.size());
        assertEquals(result.get(0), reservation);
    }

    @Test
    void testUpdate() {
        when(reservationRepository.findById(1L)).thenReturn(reservation);
        when(courtRepository.findByCourtNumber(1)).thenReturn(court);
        when(reservationRepository.overlaps(any(), any(), any(), any())).thenReturn(false);
        when(userRepository.findByPhoneNumber("0900123456")).thenReturn(user);
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Reservation result = reservationService.update(1L, request);

        assertNotNull(result);
        verify(reservationRepository).save(any());
    }

    @Test
    void testUpdate_notFound() {
        when(reservationRepository.findById(2L)).thenReturn(null); 
        
        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> reservationService.update(2L, request));

        assertEquals("Reservation not found", ex.getMessage());
    }

    @Test
    void testDelete() {
        when(reservationRepository.findById(1L)).thenReturn(reservation);
        reservationService.delete(1L);
        verify(reservationRepository).delete(1L);
    }
}
