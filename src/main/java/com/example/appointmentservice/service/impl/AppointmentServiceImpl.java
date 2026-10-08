package com.example.appointmentservice.service.impl;

import com.example.appointmentservice.dto.ApiResponseError;
import com.example.appointmentservice.entity.Appointment;
import com.example.appointmentservice.repository.AppointmentRepository;
import com.example.appointmentservice.service.AppointmentService;
import com.example.appointmentservice.service.DoctorServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final RestTemplate restTemplate;
    private final DoctorServiceClient doctorServiceClient;

    @Autowired
    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                  RestTemplate restTemplate,
                                  DoctorServiceClient doctorServiceClient) {
        this.appointmentRepository = appointmentRepository;
        this.restTemplate = restTemplate;
        this.doctorServiceClient = doctorServiceClient;
    }

    @Override
    public Object createAppointment(Appointment appointment) {
        if (appointment.getDoctorId() == null) {
            throw new IllegalArgumentException("Doctor ID must not be null");
        }

        // Verify patient existence via Patient-Service on Eureka if patientId is provided
        if (appointment.getPatientId() != null) {
            try {
                String patientUrl = "http://patient-service/api/v1/patients/" + appointment.getPatientId();
                ResponseEntity<Object> response = restTemplate.getForEntity(patientUrl, Object.class);
                if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                    throw new IllegalArgumentException("Patient with ID " + appointment.getPatientId() + " does not exist.");
                }
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                // If patient service is not reachable or patient not found
            }
        }

        // Verify doctor existence via DoctorServiceClient with CircuitBreaker & Fallback
        Object doctorResult = doctorServiceClient.getDoctor(appointment.getDoctorId());
        if (doctorResult instanceof ApiResponseError) {
            return doctorResult;
        }

        if (appointment.getAppointmentDate() == null) {
            appointment.setAppointmentDate(LocalDateTime.now());
        }

        if (appointment.getStatus() == null || appointment.getStatus().trim().isEmpty()) {
            appointment.setStatus("PENDING");
        }

        return appointmentRepository.save(appointment);
    }

    @Override
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Override
    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id).orElse(null);
    }
}
