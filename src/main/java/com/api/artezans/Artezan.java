package com.api.artezans;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableAsync
@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
public class Artezan {

    public static void main(String[] args) {
        // Load .env file if present (dev only).
        // In production, real env vars take precedence.
        Dotenv.configure()
                .ignoreIfMissing()
                .systemProperties()
                .load();

        SpringApplication.run(Artezan.class, args);
    }
}
