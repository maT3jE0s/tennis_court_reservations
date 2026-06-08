package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.dto.CourtRequest;
import com.example.demo.entity.Court;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.CourtService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(CourtController.class)
public class CourtControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourtService courtService;

    private Court court;

    @BeforeEach
    void setup() {
        court = Court.builder().courtNumber(1).build();
        court.setId(1L);
    }

    @Test
    void testCreate() throws Exception {
        CourtRequest request = new CourtRequest(1, 10L);
        when(courtService.create(any())).thenReturn(court);

        mockMvc.perform(post("/api/courts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.courtNumber").value(1));
    }

    @Test
    void testCreate_invalidRequest() throws Exception {
        CourtRequest request = new CourtRequest(null, 10L);

        mockMvc.perform(post("/api/courts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.courtNumber").value("Court number is required"));
    }

    @Test
    void testCreate_surfdaceTypeNotFound() throws Exception {
        CourtRequest request = new CourtRequest(1, 10L);
        when(courtService.create(any()))
            .thenThrow(new ResourceNotFoundException("Surface type not found"));;

        mockMvc.perform(post("/api/courts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Surface type not found"));
    }

    @Test
    void testCreate_courtWithCourtNumberExists() throws Exception {
        CourtRequest request = new CourtRequest(1, 10L);
        when(courtService.create(any()))
            .thenThrow(new IllegalArgumentException("Court with number 1 already exists"));

        mockMvc.perform(post("/api/courts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Court with number 1 already exists"));
    }

    @Test
    void testGetAll() throws Exception {
        when(courtService.getAll()).thenReturn(List.of(court));

        mockMvc.perform(get("/api/courts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].courtNumber").value(1));
    }

    @Test
    void testGetById() throws Exception {
        when(courtService.getById(1L)).thenReturn(court);

        mockMvc.perform(get("/api/courts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.courtNumber").value(1));
    }

    @Test
    void testUpdate() throws Exception {
        CourtRequest request = new CourtRequest(1, 10L);
        when(courtService.update(eq(1L), any())).thenReturn(court);

        mockMvc.perform(put("/api/courts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.courtNumber").value(1));
    }

    @Test
    void testDelete() throws Exception {
        doNothing().when(courtService).delete(1L);

        mockMvc.perform(delete("/api/courts/1"))
                .andExpect(status().isNoContent());
    }
}
