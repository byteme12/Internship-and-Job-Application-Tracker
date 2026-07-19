package com.usj.tracker.repository;

import com.usj.tracker.domain.Application;

import java.util.List;
import java.util.Optional;

/**
 * Plain interface with zero Spring/JPA dependency. The service layer talks
 * to THIS, not to ApplicationRepository directly - so the persistence
 * technology (MySQL/JPA today) can be swapped without touching business
 * logic. Deliberately kept to plain method signatures - no Page<>,
 * Specification<>, or other JPA-specific types, or the "swappable"
 * claim stops being true.
 */
public interface TrackerRepository {
    Application save(Application application);
    Optional<Application> findById(Long id);
    List<Application> findAll();
    void deleteById(Long id);
    boolean existsById(Long id);
}
