package com.assessment.config;

import com.assessment.model.RecruiterUser;
import com.assessment.repository.RecruiterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds (and reconciles) the default recruiter account on startup so the
 * password is always hashed with the configured encoder and the documented
 * credentials (admin / admin123) work out of the box.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RecruiterRepository recruiterRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public DataInitializer(RecruiterRepository recruiterRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${recruiter.admin.username:admin}") String adminUsername,
                           @Value("${recruiter.admin.password:admin123}") String adminPassword) {
        this.recruiterRepository = recruiterRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        RecruiterUser admin = recruiterRepository.findByUsername(adminUsername)
                .orElseGet(RecruiterUser::new);
        admin.setUsername(adminUsername);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRole("RECRUITER");
        recruiterRepository.save(admin);
        log.info("Recruiter account '{}' is ready", adminUsername);
    }
}
