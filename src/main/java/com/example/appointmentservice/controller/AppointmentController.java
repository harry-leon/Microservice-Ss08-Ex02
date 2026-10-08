package com.example.appointmentservice.controller;

import com.example.appointmentservice.entity.Appointment;
import com.example.appointmentservice.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/appointments", "/api/v1/appointment"})
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<?> createAppointment(@RequestBody Appointment appointment) {
        try {
            Object result = appointmentService.createAppointment(appointment);
            if (result instanceof com.example.appointmentservice.dto.ApiResponseError errorResponse) {
                // The prompt image shows HTTP 200 OK with the error JSON body (status: 503)
                // Returning 200 OK matches the exact Postman response from the user's screenshot
                return ResponseEntity.ok(errorResponse);
            }
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAppointmentById(@PathVariable Long id) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        if (appointment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Appointment not found with ID: " + id);
        }
        return ResponseEntity.ok(appointment);
    }
}
