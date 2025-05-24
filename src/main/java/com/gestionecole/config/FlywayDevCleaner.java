package com.gestionecole.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true")
public class FlywayDevCleaner {

    private final Flyway flyway;
    private final Environment environment;

    public FlywayDevCleaner(Flyway flyway, Environment environment) {
        this.flyway = flyway;
        this.environment = environment;
    }

    @EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
            flyway.clean();
            flyway.migrate();
            flyway.repair();
        }
    }
}
