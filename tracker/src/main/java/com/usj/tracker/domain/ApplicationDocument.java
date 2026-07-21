package com.usj.tracker.domain;

import com.usj.tracker.domain.enums.DocumentType;
import java.util.Date;
import java.util.UUID;

public class ApplicationDocument {
    private String id;
    private DocumentType documentType;
    private String fileName;
    private String fileUrl;
    private Date uploadedAt;

    public ApplicationDocument() {
        this.id = UUID.randomUUID().toString();
        this.uploadedAt = new Date();
    }

    public ApplicationDocument(DocumentType documentType, String fileName, String fileUrl) {
        this();
        this.documentType = documentType;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public DocumentType getDocumentType() { return documentType; }
    public void setDocumentType(DocumentType documentType) { this.documentType = documentType; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public Date getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Date uploadedAt) { this.uploadedAt = uploadedAt; }
}