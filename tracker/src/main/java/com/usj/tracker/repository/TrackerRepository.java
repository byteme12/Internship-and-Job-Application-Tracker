package com.usj.tracker.repository;

import com.usj.tracker.domain.Application;
import java.util.List;
import java.util.Optional;

// Framework-agnostic interface per the class diagram requirements
public interface TrackerRepository {
    Application save(Application application);
    Optional<Application> findById(String id);
    List<Application> findAll();
    void deleteById(String id);
    boolean existsById(String id);
}