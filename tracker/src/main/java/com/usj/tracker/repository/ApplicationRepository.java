package com.usj.tracker.repository;

import com.usj.tracker.domain.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * extends JpaRepository -> get save/findById/findAll/delete etc. for free,
 * Spring generates the real implementation as a runtime proxy; we never
 * write that class ourselves.
 *
 * extends TrackerRepository -> fulfills our own framework-agnostic contract
 * at the same time. Note the method signatures already line up
 * (JpaRepository's findById returns Optional<T>, matching TrackerRepository
 * exactly), so no adapter code is needed here.
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long>, TrackerRepository {
}
