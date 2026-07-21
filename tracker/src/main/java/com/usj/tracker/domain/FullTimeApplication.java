package com.usj.tracker.domain;

import com.usj.tracker.domain.enums.ApplicationStatus;
import org.springframework.data.annotation.TypeAlias;
import java.util.List;

@TypeAlias("FULLTIME")
public class FullTimeApplication extends Application {
    private double salary;
    private int noticePeriodDays;

    @Override
    public List<ApplicationStatus> nextValidStates() {
        return switch (getStatus()) {
            case APPLIED -> List.of(ApplicationStatus.INTERVIEW, ApplicationStatus.REJECTED);
            case INTERVIEW -> List.of(ApplicationStatus.OFFER, ApplicationStatus.REJECTED);
            case OFFER -> List.of(ApplicationStatus.ACCEPTED, ApplicationStatus.REJECTED);
            case ACCEPTED, REJECTED -> List.of();
            default -> List.of();
        };
    }

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }
    public int getNoticePeriodDays() { return noticePeriodDays; }
    public void setNoticePeriodDays(int noticePeriodDays) { this.noticePeriodDays = noticePeriodDays; }
}