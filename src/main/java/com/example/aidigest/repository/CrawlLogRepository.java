package com.example.aidigest.repository;

import com.example.aidigest.model.CrawlLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrawlLogRepository extends JpaRepository<CrawlLog, Long> {
}
