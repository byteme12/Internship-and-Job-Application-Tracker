package com.usj.tracker.repository;

import com.usj.tracker.domain.Application;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends MongoRepository<Application, String>, TrackerRepository {
    // Spring Data MongoDB generates the implementation
}