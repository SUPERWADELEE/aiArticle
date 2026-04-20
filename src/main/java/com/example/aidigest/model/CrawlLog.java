package com.example.aidigest.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "crawl_logs")
public class CrawlLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant finishedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CrawlStatus status;

    private Integer fetchedCount;
    private Integer filteredCount;
    private Integer newCount;
    private Integer savedCount;
    private Integer summarizeFailedCount;

    private Long durationMs;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String sourceErrors;

    @Column(length = 16)
    private String triggeredBy;

    public CrawlLog() {}

    public Long getId() { return id; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }

    public CrawlStatus getStatus() { return status; }
    public void setStatus(CrawlStatus status) { this.status = status; }

    public Integer getFetchedCount() { return fetchedCount; }
    public void setFetchedCount(Integer fetchedCount) { this.fetchedCount = fetchedCount; }

    public Integer getFilteredCount() { return filteredCount; }
    public void setFilteredCount(Integer filteredCount) { this.filteredCount = filteredCount; }

    public Integer getNewCount() { return newCount; }
    public void setNewCount(Integer newCount) { this.newCount = newCount; }

    public Integer getSavedCount() { return savedCount; }
    public void setSavedCount(Integer savedCount) { this.savedCount = savedCount; }

    public Integer getSummarizeFailedCount() { return summarizeFailedCount; }
    public void setSummarizeFailedCount(Integer summarizeFailedCount) { this.summarizeFailedCount = summarizeFailedCount; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getSourceErrors() { return sourceErrors; }
    public void setSourceErrors(String sourceErrors) { this.sourceErrors = sourceErrors; }

    public String getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; }
}
