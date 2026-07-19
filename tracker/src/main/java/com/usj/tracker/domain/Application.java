package com.usj.tracker.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.usj.tracker.exception.InvalidTransitionException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

/**
 * Abstract root of the application hierarchy. Never instantiated directly -
 * every real document is either an InternshipApplication or a
 * FullTimeApplication.
 *
 * Persisted with Spring Data MongoDB: all three classes share ONE
 * collection ("applications"), and Spring Data automatically writes a
 * "_class" field into every document to record which concrete subclass
 * it is, then uses that field to deserialize back to the correct type on
 * read. Unlike JPA, there's no manual @Inheritance/@DiscriminatorColumn
 * to declare - it's automatic once every subclass just extends this one.
 */
@Document(collection = "applications")
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "applicationType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = InternshipApplication.class, name = "INTERNSHIP"),
        @JsonSubTypes.Type(value = FullTimeApplication.class, name = "FULLTIME")
})
public abstract class Application {

    @Id
    private String id; // Mongo IDs are Strings (ObjectId hex), not Long

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotNull(message = "Date applied is required")
    private LocalDate dateApplied;

    private ApplicationStatus status;

    protected Application() {
        // required by the persistence framework
    }

    protected Application(String companyName, LocalDate dateApplied) {
        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException("Company name cannot be blank");
        }
        if (dateApplied == null) {
            throw new IllegalArgumentException("Date applied cannot be null");
        }
        this.companyName = companyName;
        this.dateApplied = dateApplied;
        this.status = ApplicationStatus.APPLIED; // every application starts here
    }

    public abstract List<ApplicationStatus> nextValidStates();

    public void transitionTo(ApplicationStatus target) {
        if (target == null) {
            throw new IllegalArgumentException("Target status cannot be null");
        }
        if (!nextValidStates().contains(target)) {
            throw new InvalidTransitionException(
                    "Cannot transition from " + this.status + " to " + target +
                    " for " + this.getClass().getSimpleName());
        }
        this.status = target;
    }

    public String getId() {
        return id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException("Company name cannot be blank");
        }
        this.companyName = companyName;
    }

    public LocalDate getDateApplied() {
        return dateApplied;
    }

    public ApplicationStatus getStatus() {
        return status;
    }
}
