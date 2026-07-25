package com.usj.tracker.service;

import com.usj.tracker.domain.Application;
import com.usj.tracker.domain.ApplicationDocument;
import com.usj.tracker.domain.FullTimeApplication;
import com.usj.tracker.domain.InternshipApplication;
import com.usj.tracker.domain.enums.ApplicationStatus;
import com.usj.tracker.domain.enums.DocumentType;
import com.usj.tracker.exception.ApplicationNotFoundException;
import com.usj.tracker.repository.TrackerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class ApplicationService {

    private final TrackerRepository trackerRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public ApplicationService(TrackerRepository trackerRepository) {
        this.trackerRepository = trackerRepository;
    }

    public Application createApplication(Application app) {
        validateEditableFields(app);
        return trackerRepository.save(app);
    }

    public Application updateApplication(String id, Application incoming) {
        Application existing = getApplicationById(id);

        if (!existing.getClass().equals(incoming.getClass())) {
            throw new IllegalArgumentException("Cannot change application type on an existing application");
        }
        validateEditableFields(incoming);

        existing.setCompanyName(incoming.getCompanyName());
        existing.setCompanyId(incoming.getCompanyId());
        existing.setJobRole(incoming.getJobRole());
        existing.setDateApplied(incoming.getDateApplied());

        if (existing instanceof InternshipApplication existingInternship
                && incoming instanceof InternshipApplication incomingInternship) {
            existingInternship.setDurationMonths(incomingInternship.getDurationMonths());
            existingInternship.setStipend(incomingInternship.getStipend());
            existingInternship.setUniversity(incomingInternship.getUniversity());
        } else if (existing instanceof FullTimeApplication existingFullTime
                && incoming instanceof FullTimeApplication incomingFullTime) {
            existingFullTime.setSalary(incomingFullTime.getSalary());
            existingFullTime.setNoticePeriodDays(incomingFullTime.getNoticePeriodDays());
        }

        return trackerRepository.save(existing);
    }

    private void validateEditableFields(Application app) {
        if (app.getCompanyName() == null || app.getCompanyName().isBlank()) {
            throw new IllegalArgumentException("Company name cannot be blank");
        }
        if (app.getJobRole() == null || app.getJobRole().isBlank()) {
            throw new IllegalArgumentException("Job role cannot be blank");
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

    public Application uploadDocument(String id, DocumentType documentType, MultipartFile file) {
        if (documentType == null) {
            throw new IllegalArgumentException("Document type is required");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        Application app = getApplicationById(id);

        String originalFileName = Paths.get(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "file"
        ).getFileName().toString();
        String storedFileName = UUID.randomUUID() + "-" + originalFileName;

        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(storedFileName));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store uploaded file: " + e.getMessage(), e);
        }

        ApplicationDocument document = new ApplicationDocument(documentType, originalFileName, "/uploads/" + storedFileName);
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