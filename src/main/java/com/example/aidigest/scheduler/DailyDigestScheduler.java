package com.example.aidigest.scheduler;

import com.example.aidigest.service.DigestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyDigestScheduler {

    private static final Logger log = LoggerFactory.getLogger(DailyDigestScheduler.class);

    private final DigestService digestService;

    public DailyDigestScheduler(DigestService digestService) {
        this.digestService = digestService;
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void runDailyDigest() {
        log.info("Daily digest scheduler triggered");
        digestService.runDigest("cron");
    }
}
