package com.usj.tracker.domain;

import com.usj.tracker.domain.enums.ApplicationStatus;
import com.usj.tracker.exception.InvalidTransitionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationDomainTest {

    @Test
    void testInternshipValidTransitions() {
        Application app = new InternshipApplication();
        assertEquals(ApplicationStatus.APPLIED, app.getStatus());

        assertDoesNotThrow(() -> app.transitionTo(ApplicationStatus.INTERVIEW));
        assertEquals(ApplicationStatus.INTERVIEW, app.getStatus());

        assertDoesNotThrow(() -> app.transitionTo(ApplicationStatus.OFFER));
        assertEquals(ApplicationStatus.OFFER, app.getStatus());

        assertDoesNotThrow(() -> app.transitionTo(ApplicationStatus.ACCEPTED));
        assertEquals(ApplicationStatus.ACCEPTED, app.getStatus());

        assertDoesNotThrow(() -> app.transitionTo(ApplicationStatus.CONVERTED_TO_FULLTIME));
        assertEquals(ApplicationStatus.CONVERTED_TO_FULLTIME, app.getStatus());
        
        // Converted is a terminal state[cite: 1]
        assertTrue(app.nextValidStates().isEmpty()); 
    }

    @Test
    void testInternshipInvalidTransition() {
        Application app = new InternshipApplication();
        assertEquals(ApplicationStatus.APPLIED, app.getStatus());

        InvalidTransitionException exception = assertThrows(InvalidTransitionException.class, 
                () -> app.transitionTo(ApplicationStatus.OFFER));
                
        assertTrue(exception.getMessage().contains("Cannot transition from APPLIED to OFFER"));
    }

    @Test
    void testFullTimeValidTransitionsAndTerminalState() {
        Application app = new FullTimeApplication();
        assertEquals(ApplicationStatus.APPLIED, app.getStatus());

        app.transitionTo(ApplicationStatus.INTERVIEW);
        app.transitionTo(ApplicationStatus.REJECTED);
        
        // Rejected is a terminal state[cite: 1]
        assertTrue(app.nextValidStates().isEmpty());
    }

    @Test
    void testFullTimeCannotConvert() {
        Application app = new FullTimeApplication();
        app.transitionTo(ApplicationStatus.INTERVIEW);
        app.transitionTo(ApplicationStatus.OFFER);
        app.transitionTo(ApplicationStatus.ACCEPTED);
        
        // Accepted is a terminal state for FullTime[cite: 1]
        assertTrue(app.nextValidStates().isEmpty());

        assertThrows(InvalidTransitionException.class, 
                () -> app.transitionTo(ApplicationStatus.CONVERTED_TO_FULLTIME));
    }
}