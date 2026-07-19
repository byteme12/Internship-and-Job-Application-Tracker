package com.usj.tracker.service;

import com.usj.tracker.domain.Application;
import com.usj.tracker.domain.ApplicationStatus;
import com.usj.tracker.exception.ApplicationNotFoundException;
import com.usj.tracker.repository.TrackerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Depends on TrackerRepository (the interface), never on ApplicationRepository
 * or JpaRepository directly. This is what makes the "swap persistence
 * without touching business logic" claim real rather than decorative.
 *
 * Notice there is no instanceof / type-checking anywhere in here, even
 * though Application objects flowing through are actually a mix of
 * InternshipApplication and FullTimeApplication. transitionTo() and
 * nextValidStates() are resolved polymorphically at runtime.
 */
@Service
public class ApplicationService {

    private final TrackerRepository repository;

    public ApplicationService(TrackerRepository repository) {
        this.repository = repository;
    }

    public Application create(Application application) {
        return repository.save(application);
    }

    public List<Application> findAll() {
        return repository.findAll();
    }

    public Application findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));
    }

    /**
     * Loads the entity, asks IT what it's allowed to do next (polymorphic
     * call - no branching on type here), and lets transitionTo() throw
     * InvalidTransitionException if the move is illegal.
     */
    public Application transition(Long id, ApplicationStatus target) {
        Application application = findById(id);
        application.transitionTo(target);
        return repository.save(application);
    }

    public List<ApplicationStatus> getValidNextStates(Long id) {
        return findById(id).nextValidStates();
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ApplicationNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
