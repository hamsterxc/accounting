package com.lonebytesoft.hamster.accounting.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Arrays;

@SpringBootApplication
@ComponentScan("com.lonebytesoft.hamster.accounting")
@EnableJpaRepositories("com.lonebytesoft.hamster.accounting.repository")
@EntityScan("com.lonebytesoft.hamster.accounting.model")
public class Accounting {

    private static final String PROFILE_SETUP = "setup";

    public static void main(String[] args) {
        final ApplicationContext applicationContext = SpringApplication.run(Accounting.class, args);

        if(Arrays.stream(applicationContext.getEnvironment().getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase(PROFILE_SETUP))) {
            SpringApplication.exit(applicationContext);
        }
    }

}
