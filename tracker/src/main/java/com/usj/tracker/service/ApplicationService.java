package com.usj.tracker.service;

import com.usj.tracker.domain.Application;
import com.usj.tracker.domain.ApplicationDocument;
import com.usj.tracker.domain.FullTimeApplication;
import com.usj.tracker.domain.InternshipApplication;
import com.usj.tracker.domain.enums.ApplicationStatus;
import com.usj.tracker.exception.ApplicationNotFoundException;
import com.usj.tracker.repository.TrackerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationService {

    private final TrackerRepository trackerRepository;

    public ApplicationService(TrackerRepository trackerRepository) {
        this.trackerRepository = trackerRepository;
    }

    public Application createApplication(Application app) {
        if (app.getCompanyName() == null || app.getCompanyName().isBlank()) {
            throw new IllegalArgumentException("Company name cannot be blank");
        }
        if (app.getDateApplied() == null) {
            throw new IllegalArgumentException("Date applied is required");
        }

        if (app instanceof InternshipApplication internship) {
            if (internship.getDurationMonths() <= 0) throw new IllegalArgumentException("Duration must be > 0");
            if (internship.getStipend() < 0) throw new IllegalArgumentException("Stipend cannot be negative");
        } else if (app instanceof FullTimeApplication fullTime) {
            if (fullTime.getSalary() < 0) throw new IllegalArgumentException("Salary cannot be negative");
            if (fullTime.getNoticePeriodDays() < 0) throw new IllegalArgumentException("Notice period cannot be negative");
        }

        return trackerRepository.save(app);
    }

    public List<Application> getAllApplications() {
        return trackerRepository.findAll();
    }

    public Application getApplicationById(String id) {
        return trackerRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with id: " + id));
    }

    public List<ApplicationStatus> getNextStates(String id) {
        Application app = getApplicationById(id);
        return app.nextValidStates(); // Polymorphic call
    }

    public Application transitionStatus(String id, ApplicationStatus newStatus) {
        Application app = getApplicationById(id);
        app.transitionTo(newStatus); // Validates and transitions polymorphically
        return trackerRepository.save(app);
    }

    public Application addDocument(String id, ApplicationDocument document) {
        if (document.getDocumentType() == null) {
            throw new IllegalArgumentException("Document type is required");
        }
        if (document.getFileName() == null || document.getFileName().isBlank()) {
            throw new IllegalArgumentException("File name cannot be blank");
        }

        Application app = getApplicationById(id);
        app.addDocument(document);
        return trackerRepository.save(app);
    }

    public void deleteApplication(String id) {
        if (!trackerRepository.existsById(id)) {
            throw new ApplicationNotFoundException("Application not found with id: " + id);
        }
        trackerRepository.deleteById(id);
    }
}