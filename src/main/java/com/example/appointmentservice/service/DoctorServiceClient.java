package com.example.appointmentservice.service;

import com.example.appointmentservice.dto.ApiResponseError;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
public class DoctorServiceClient {

    private final RestTemplate restTemplate;

    @Autowired
    public DoctorServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "doctorServiceCB", fallbackMethod = "getDoctorFallback")
    public Object getDoctor(Long doctorId) {
        String doctorUrl = "http://doctor-service/api/v1/doctors/" + doctorId;
        ResponseEntity<Object> response = restTemplate.getForEntity(doctorUrl, Object.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Doctor with ID " + doctorId + " does not exist.");
        }
        return response.getBody();
    }

    public Object getDoctorFallback(Long doctorId, Exception e) {
        return new ApiResponseError(
                "Hiện tại không thể kiểm tra thông tin bác sĩ, vui lòng thử lại sau vài giây",
                503,
                "Doctor Service Error",
                LocalDateTime.now()
        );
    }

    public Object getDoctorFallback(Exception e) {
        return new ApiResponseError(
                "Hiện tại không thể kiểm tra thông tin bác sĩ, vui lòng thử lại sau vài giây",
                503,
                "Doctor Service Error",
                LocalDateTime.now()
        );
    }
}
