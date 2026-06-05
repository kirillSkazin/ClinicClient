package org.example.clinic.client.service;

import org.example.clinic.client.network.ServerClient;


public class Services {

    private final ServerClient client;
    private final AuthService auth;
    private final SpecializationService specializations;
    private final DoctorService doctors;
    private final PatientService patients;
    private final ScheduleService schedules;
    private final AppointmentService appointments;
    private final MedicalRecordService records;
    private final StatisticsService statistics;

    public Services(ServerClient client) {
        this.client = client;
        this.auth = new AuthService(client);
        this.specializations = new SpecializationService(client);
        this.doctors = new DoctorService(client);
        this.patients = new PatientService(client);
        this.schedules = new ScheduleService(client);
        this.appointments = new AppointmentService(client);
        this.records = new MedicalRecordService(client);
        this.statistics = new StatisticsService(client);
    }

    public ServerClient client() { return client; }
    public AuthService auth() { return auth; }
    public SpecializationService specializations() { return specializations; }
    public DoctorService doctors() { return doctors; }
    public PatientService patients() { return patients; }
    public ScheduleService schedules() { return schedules; }
    public AppointmentService appointments() { return appointments; }
    public MedicalRecordService records() { return records; }
    public StatisticsService statistics() { return statistics; }
}
