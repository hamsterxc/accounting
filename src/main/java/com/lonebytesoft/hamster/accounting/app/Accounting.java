package com.lonebytesoft.hamster.accounting.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan("com.lonebytesoft.hamster.accounting")
@EnableJpaRepositories("com.lonebytesoft.hamster.accounting.repository")
@EntityScan("com.lonebytesoft.hamster.accounting.model")
public class Accounting {

    public static void main(String[] args) {
        SpringApplication.run(Accounting.class, args);
    }

}
