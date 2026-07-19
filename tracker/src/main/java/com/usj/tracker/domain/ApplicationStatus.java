package com.usj.tracker.domain;

/**
 * All possible statuses across both application types.
 * Which of these are reachable from which state is decided per-subclass
 * inside Application#nextValidStates() - NOT here. This enum only lists
 * the vocabulary; the subclasses own the rules.
 */
public enum ApplicationStatus {
    APPLIED,
    INTERVIEW,
    OFFER,
    ACCEPTED,
    REJECTED,
    CONVERTED_TO_FULLTIME // reachable only from ACCEPTED, and only for InternshipApplication
}
