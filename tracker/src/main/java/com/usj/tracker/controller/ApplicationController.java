package com.usj.tracker.controller;

import com.usj.tracker.domain.Application;
import com.usj.tracker.domain.ApplicationStatus;
import com.usj.tracker.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Frontend posts JSON like:
 *   { "applicationType": "INTERNSHIP", "companyName": "...", "dateApplied": "2026-07-01",
 *     "durationMonths": 3, "stipend": 500, "university": "USJ" }
 * or
 *   { "applicationType": "FULLTIME", "companyName": "...", "dateApplied": "2026-07-01",
 *     "salary": 120000, "noticePeriodDays": 30 }
 *
 * The "applicationType" field is read by Jackson (see @JsonTypeInfo on
 * Application) BEFORE this method body ever runs, so by the time
 * `application` arrives here it's already the correct concrete subclass -
 * this controller never checks which one.
 */
@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*") // relax for coursework; tighten in a real deployment
public class ApplicationController {

    private final ApplicationService service;

    public ApplicationController(ApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Application> create(@Valid @RequestBody Application application) {
        Application saved = service.create(application);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public List<Application> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Application findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/{id}/next-states")
    public List<ApplicationStatus> nextStates(@PathVariable Long id) {
        return service.getValidNextStates(id);
    }

    /**
     * Body: { "status": "INTERVIEW" }
     * Delegates straight to the polymorphic transitionTo() - if the target
     * status isn't legal for this object's actual type/current state,
     * InvalidTransitionException bubbles up and GlobalExceptionHandler
     * turns it into a 409.
     */
    @PatchMapping("/{id}/transition")
    public Application transition(@PathVariable Long id, @RequestBody Map<String, String> body) {
        ApplicationStatus target = ApplicationStatus.valueOf(body.get("status"));
        return service.transition(id, target);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
