package com.example.appointmentservice.service;

import com.example.appointmentservice.entity.Appointment;

import java.util.List;

public interface AppointmentService {
    Object createAppointment(Appointment appointment);
    List<Appointment> getAllAppointments();
    Appointment getAppointmentById(Long id);
}
