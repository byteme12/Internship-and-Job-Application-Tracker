package com.usj.tracker.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDate;
import java.util.List;

@Entity
@DiscriminatorValue("INTERNSHIP")
public class InternshipApplication extends Application {

    private int durationMonths;
    private double stipend;
    private String university;

    protected InternshipApplication() {
        super();
    }

    public InternshipApplication(String companyName, LocalDate dateApplied,
                                  int durationMonths, double stipend, String university) {
        super(companyName, dateApplied);
        if (durationMonths <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        if (stipend < 0) {
            throw new IllegalArgumentException("Stipend cannot be negative");
        }
        this.durationMonths = durationMonths;
        this.stipend = stipend;
        this.university = university;
    }

    /**
     * Internship-specific state machine:
     * APPLIED -> INTERVIEW -> OFFER -> ACCEPTED -> (optionally) CONVERTED_TO_FULLTIME
     * REJECTED is reachable from APPLIED, INTERVIEW, or OFFER.
     * Conversion is NOT guaranteed - ACCEPTED is a valid terminal state on its own.
     */
    @Override
    public List<ApplicationStatus> nextValidStates() {
        return switch (getStatus()) {
            case APPLIED -> List.of(ApplicationStatus.INTERVIEW, ApplicationStatus.REJECTED);
            case INTERVIEW -> List.of(ApplicationStatus.OFFER, ApplicationStatus.REJECTED);
            case OFFER -> List.of(ApplicationStatus.ACCEPTED, ApplicationStatus.REJECTED);
            case ACCEPTED -> List.of(ApplicationStatus.CONVERTED_TO_FULLTIME); // optional next step
            case CONVERTED_TO_FULLTIME, REJECTED -> List.of(); // terminal
        };
    }

    public int getDurationMonths() {
        return durationMonths;
    }

    public double getStipend() {
        return stipend;
    }

    public String getUniversity() {
        return university;
    }
}
