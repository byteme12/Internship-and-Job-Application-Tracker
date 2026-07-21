package com.usj.tracker.domain;

import com.usj.tracker.domain.enums.ApplicationStatus;
import org.springframework.data.annotation.TypeAlias;
import java.util.List;

@TypeAlias("INTERNSHIP")
public class InternshipApplication extends Application {
    private int durationMonths;
    private double stipend;
    private String university;

    @Override
    public List<ApplicationStatus> nextValidStates() {
        return switch (getStatus()) {
            case APPLIED -> List.of(ApplicationStatus.INTERVIEW, ApplicationStatus.REJECTED);
            case INTERVIEW -> List.of(ApplicationStatus.OFFER, ApplicationStatus.REJECTED);
            case OFFER -> List.of(ApplicationStatus.ACCEPTED, ApplicationStatus.REJECTED);
            case ACCEPTED -> List.of(ApplicationStatus.CONVERTED_TO_FULLTIME);
            case CONVERTED_TO_FULLTIME, REJECTED -> List.of();
        };
    }

    public int getDurationMonths() { return durationMonths; }
    public void setDurationMonths(int durationMonths) { this.durationMonths = durationMonths; }
    public double getStipend() { return stipend; }
    public void setStipend(double stipend) { this.stipend = stipend; }
    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }
}