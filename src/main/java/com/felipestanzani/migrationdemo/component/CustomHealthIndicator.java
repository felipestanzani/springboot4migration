package com.felipestanzani.migrationdemo.component;

// Libs do Actuator foram movidas de pacote
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CustomHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        var serviceUp = checkService();

        if (serviceUp) {
            return Health.up().withDetail("status", "Alive and kicking!!!").build();
        }
        return Health.down().withDetail("error", "I'm feeling bad...").build();
    }

    private boolean checkService() {
        return Math.random() > 0.2;
    }
}
