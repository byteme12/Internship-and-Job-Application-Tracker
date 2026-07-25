package com.usj.tracker.controller;

import com.usj.tracker.domain.Application;
import com.usj.tracker.domain.ApplicationDocument;
import com.usj.tracker.domain.enums.ApplicationStatus;
import com.usj.tracker.domain.enums.DocumentType;
import com.usj.tracker.dto.TransitionRequest;
import com.usj.tracker.service.ApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<Application> createApplication(@RequestBody Application application) {
        Application created = applicationService.createApplication(application);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Application>> getAllApplications() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Application> getApplicationById(@PathVariable String id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Application> updateApplication(@PathVariable String id, @RequestBody Application application) {
        return ResponseEntity.ok(applicationService.updateApplication(id, application));
    }

    @GetMapping("/{id}/next-states")
    public ResponseEntity<List<ApplicationStatus>> getNextStates(@PathVariable String id) {
        return ResponseEntity.ok(applicationService.getNextStates(id));
    }

    @PatchMapping("/{id}/transition")
    public ResponseEntity<Application> transitionApplication(@PathVariable String id, @RequestBody TransitionRequest request) {
        Application updated = applicationService.transitionStatus(id, request.getStatus());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/documents")
    public ResponseEntity<Application> addDocument(@PathVariable String id, @RequestBody ApplicationDocument document) {
        Application updated = applicationService.addDocument(id, document);
        return new ResponseEntity<>(updated, HttpStatus.CREATED);
    }

    @PostMapping(value = "/{id}/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Application> uploadDocument(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") DocumentType documentType) {
        Application updated = applicationService.uploadDocument(id, documentType, file);
        return new ResponseEntity<>(updated, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable String id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }
}