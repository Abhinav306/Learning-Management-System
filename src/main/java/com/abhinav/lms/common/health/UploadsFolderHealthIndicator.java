package com.abhinav.lms.common.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class UploadsFolderHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        File uploadDir = new File("uploads");
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (!created) {
                return Health.down()
                        .withDetail("uploads_folder", "uploads directory is missing and cannot be created")
                        .build();
            }
        }

        if (!uploadDir.canWrite()) {
            return Health.down()
                    .withDetail("uploads_folder", "uploads directory is not writable")
                    .build();
        }

        return Health.up()
                .withDetail("uploads_folder", "uploads directory exists and is writable")
                .build();
    }
}
