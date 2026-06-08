package com.example.demo.service;

import java.time.Duration;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ReservationRequest;
import com.example.demo.entity.Court;
import com.example.demo.entity.Reservation;
import com.example.demo.entity.Reservation.GameType;
import com.example.demo.entity.SurfaceType;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.CourtRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private static final double DOUBLES_MULTIPLIER = 1.5;

    private final ReservationRepository reservationRepository;
    private final CourtRepository courtRepository;
    private final UserRepository userRepository;

    @Transactional
    public Reservation create(ReservationRequest request) {
        Court court = requireCourt(request);
        
        checkTimeInterval(court.getId(), request, null);

        User user = getUser(request);

        Reservation reservation = Reservation.builder()
            .court(court)
            .user(user)
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .gameType(request.getGameType())
            .totalPrice(calculatePrice(court.getSurfaceType(), request))
            .build();

        return reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public Reservation getById(Long id) {
        return requireReservation(id);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getByCourtNumber(Integer courtNumber) {
        return reservationRepository.findByCourtNumber(courtNumber);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getByPhoneNumber(String phoneNumber, boolean futureOnly) {
        return reservationRepository.findByPhoneNumber(phoneNumber, futureOnly);
    }

    @Transactional
    public Reservation update(Long id, ReservationRequest request) {
        Reservation reservation = requireReservation(id);
        Court court = requireCourt(request);

        checkTimeInterval(court.getId(), request, id);

        User user = getUser(request);

        reservation.setCourt(court);
        reservation.setUser(user);
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setGameType(request.getGameType());
        reservation.setTotalPrice(calculatePrice(court.getSurfaceType(), request));

        return reservationRepository.save(reservation);
    }

    @Transactional
    public void delete(Long id) {
        requireReservation(id);
        reservationRepository.delete(id);
    }

    private double calculatePrice(SurfaceType surfaceType, ReservationRequest request) {
        long minutes = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        double base = surfaceType.getPricePerMinute() * minutes;

        if (request.getGameType() == GameType.DOUBLE) {
            base *= DOUBLES_MULTIPLIER;
        }

        return base;
    }

    private Reservation requireReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id);
        if (reservation == null)
            throw new ResourceNotFoundException("Reservation not found");

        return reservation;
    }

    private Court requireCourt(ReservationRequest request) {
        Court court = courtRepository.findByCourtNumber(request.getCourtNumber());
        if (court == null)
            throw new ResourceNotFoundException("Court not found");

        return court;
    }

    private void checkTimeInterval(Long courtId, ReservationRequest request, Long excludeId) {
        if (request.getStartTime().isAfter(request.getEndTime()))
            throw new IllegalArgumentException("Reservation must start before it ends");
        if (reservationRepository.overlaps(courtId, request.getStartTime(), request.getEndTime(), excludeId))
            throw new IllegalArgumentException("The time interval overlaps with an existing reservation");
    }

    private User getUser(ReservationRequest request) {
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber());
        if (user == null) {
            user = new User(request.getPhoneNumber(), request.getUserName());
            userRepository.save(user);
        }

        return user;
    }
}
