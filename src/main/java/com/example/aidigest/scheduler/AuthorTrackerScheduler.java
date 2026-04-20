package com.example.aidigest.scheduler;

import com.example.aidigest.service.AuthorActivityTracker;
import com.example.aidigest.service.TelegramService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AuthorTrackerScheduler {

    private static final Logger log = LoggerFactory.getLogger(AuthorTrackerScheduler.class);

    private final AuthorActivityTracker tracker;
    private final TelegramService telegramService;

    public AuthorTrackerScheduler(AuthorActivityTracker tracker, TelegramService telegramService) {
        this.tracker = tracker;
        this.telegramService = telegramService;
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void runDailyAuthorTrack() {
        if (!tracker.isEnabled()) {
            log.info("Author tracker disabled, skipping scheduled run");
            return;
        }
        log.info("Author tracker scheduler triggered");
        AuthorActivityTracker.Report report = tracker.runDailyCheck();
        telegramService.sendAuthorActivityReport(report);
    }
}
