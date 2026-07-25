package com.usj.tracker.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.usj.tracker.domain.enums.ApplicationStatus;
import com.usj.tracker.exception.InvalidTransitionException;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    private String id;
    private String companyName;
    private String jobRole;
    private LocalDate dateApplied;
    private ApplicationStatus status;
    private List<ApplicationDocument> documents = new ArrayList<>();
    private String companyId;

    public Application() {
        this.status = ApplicationStatus.APPLIED;
    }

    public abstract List<ApplicationStatus> nextValidStates();

    public void transitionTo(ApplicationStatus target) {
        if (!nextValidStates().contains(target)) {
            throw new InvalidTransitionException("Cannot transition from " + this.status + " to " + target + " for " + this.getClass().getSimpleName());
        }
        this.status = target;
    }

    public void addDocument(ApplicationDocument doc) {
        if (this.documents == null) {
            this.documents = new ArrayList<>();
        }
        this.documents.add(doc);
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getJobRole() { return jobRole; }
    public void setJobRole(String jobRole) { this.jobRole = jobRole; }
    public LocalDate getDateApplied() { return dateApplied; }
    public void setDateApplied(LocalDate dateApplied) { this.dateApplied = dateApplied; }
    public ApplicationStatus getStatus() { return status; }
    // No public setter for status per requirements!
    public List<ApplicationDocument> getDocuments() { return documents; }
    public void setDocuments(List<ApplicationDocument> documents) { this.documents = documents; }
    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }
}