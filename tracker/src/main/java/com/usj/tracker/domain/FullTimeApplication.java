package com.usj.tracker.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDate;
import java.util.List;

@Entity
@DiscriminatorValue("FULLTIME")
public class FullTimeApplication extends Application {

    private double salary;
    private int noticePeriodDays;

    protected FullTimeApplication() {
        super();
    }

    public FullTimeApplication(String companyName, LocalDate dateApplied,
                                double salary, int noticePeriodDays) {
        super(companyName, dateApplied);
        if (salary < 0) {
            throw new IllegalArgumentException("Salary cannot be negative");
        }
        if (noticePeriodDays < 0) {
            throw new IllegalArgumentException("Notice period cannot be negative");
        }
        this.salary = salary;
        this.noticePeriodDays = noticePeriodDays;
    }

    /**
     * Full-time state machine: APPLIED -> INTERVIEW -> OFFER -> ACCEPTED.
     * No CONVERTED_TO_FULLTIME - there's nothing to convert from.
     * REJECTED reachable from APPLIED, INTERVIEW, or OFFER.
     */
    @Override
    public List<ApplicationStatus> nextValidStates() {
        return switch (getStatus()) {
            case APPLIED -> List.of(ApplicationStatus.INTERVIEW, ApplicationStatus.REJECTED);
            case INTERVIEW -> List.of(ApplicationStatus.OFFER, ApplicationStatus.REJECTED);
            case OFFER -> List.of(ApplicationStatus.ACCEPTED, ApplicationStatus.REJECTED);
            case ACCEPTED, REJECTED -> List.of(); // terminal
            case CONVERTED_TO_FULLTIME -> List.of(); // never valid for this subclass
        };
    }

    public double getSalary() {
        return salary;
    }

    public int getNoticePeriodDays() {
        return noticePeriodDays;
    }
}
