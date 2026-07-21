package com.usj.tracker.dto;

import com.usj.tracker.domain.enums.ApplicationStatus;

public class TransitionRequest {
    private ApplicationStatus status;

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
}