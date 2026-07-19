package com.usj.tracker.domain;

import com.usj.tracker.exception.InvalidTransitionException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pure domain-level tests - no Spring context, no database. Runs fast and
 * proves the polymorphic state machine is correct before anything else
 * gets built on top of it.
 */
class ApplicationTransitionTest {

    private InternshipApplication newInternship() {
        return new InternshipApplication("Sysco LABS", LocalDate.now(), 3, 50000, "USJ");
    }

    private FullTimeApplication newFullTime() {
        return new FullTimeApplication("Sysco LABS", LocalDate.now(), 250000, 30);
    }

    @Test
    void newApplicationStartsAsApplied() {
        assertThat(newInternship().getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(newFullTime().getStatus()).isEqualTo(ApplicationStatus.APPLIED);
    }

    @Test
    void internshipCannotSkipStagesFromAppliedToOffer() {
        InternshipApplication app = newInternship();
        assertThatThrownBy(() -> app.transitionTo(ApplicationStatus.OFFER))
                .isInstanceOf(InvalidTransitionException.class);
    }

    @Test
    void internshipCanReachConvertedToFulltimeThroughFullPath() {
        InternshipApplication app = newInternship();
        app.transitionTo(ApplicationStatus.INTERVIEW);
        app.transitionTo(ApplicationStatus.OFFER);
        app.transitionTo(ApplicationStatus.ACCEPTED);
        app.transitionTo(ApplicationStatus.CONVERTED_TO_FULLTIME);
        assertThat(app.getStatus()).isEqualTo(ApplicationStatus.CONVERTED_TO_FULLTIME);
    }

    @Test
    void internshipAcceptedIsValidTerminalStateWithoutConversion() {
        InternshipApplication app = newInternship();
        app.transitionTo(ApplicationStatus.INTERVIEW);
        app.transitionTo(ApplicationStatus.OFFER);
        app.transitionTo(ApplicationStatus.ACCEPTED);
        assertThat(app.nextValidStates()).containsExactly(ApplicationStatus.CONVERTED_TO_FULLTIME);
        // not every intern converts - simply never calling transitionTo again is valid
    }

    @Test
    void fullTimeApplicationCanNeverConvert() {
        FullTimeApplication app = newFullTime();
        app.transitionTo(ApplicationStatus.INTERVIEW);
        app.transitionTo(ApplicationStatus.OFFER);
        app.transitionTo(ApplicationStatus.ACCEPTED);
        assertThatThrownBy(() -> app.transitionTo(ApplicationStatus.CONVERTED_TO_FULLTIME))
                .isInstanceOf(InvalidTransitionException.class);
    }

    @Test
    void rejectedIsTerminalForBothTypes() {
        InternshipApplication internship = newInternship();
        internship.transitionTo(ApplicationStatus.REJECTED);
        assertThat(internship.nextValidStates()).isEmpty();

        FullTimeApplication fullTime = newFullTime();
        fullTime.transitionTo(ApplicationStatus.REJECTED);
        assertThat(fullTime.nextValidStates()).isEmpty();
    }

    @Test
    void polymorphicDispatchPicksCorrectSubclassRules() {
        // Same reference type (Application), different runtime types -
        // this is the actual polymorphism the whole design hinges on.
        Application internship = newInternship();
        Application fullTime = newFullTime();

        internship.transitionTo(ApplicationStatus.INTERVIEW);
        internship.transitionTo(ApplicationStatus.OFFER);
        internship.transitionTo(ApplicationStatus.ACCEPTED);
        assertThat(internship.nextValidStates()).contains(ApplicationStatus.CONVERTED_TO_FULLTIME);

        fullTime.transitionTo(ApplicationStatus.INTERVIEW);
        fullTime.transitionTo(ApplicationStatus.OFFER);
        fullTime.transitionTo(ApplicationStatus.ACCEPTED);
        assertThat(fullTime.nextValidStates()).doesNotContain(ApplicationStatus.CONVERTED_TO_FULLTIME);
    }

    @Test
    void constructorRejectsBlankCompanyName() {
        assertThatThrownBy(() -> new InternshipApplication("", LocalDate.now(), 3, 500, "USJ"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructorRejectsNegativeStipend() {
        assertThatThrownBy(() -> new InternshipApplication("Sysco", LocalDate.now(), 3, -100, "USJ"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
