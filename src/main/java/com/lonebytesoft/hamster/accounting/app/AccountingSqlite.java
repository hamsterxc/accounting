package com.lonebytesoft.hamster.accounting.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

import java.io.File;

public class AccountingSqlite {

    private static final Logger logger = LoggerFactory.getLogger(AccountingSqlite.class);

    private static final String DEFAULT_DATABASE_NAME = "accounting.db";

    public static void main(String[] args) {
        final String filename;
        if (args.length > 0) {
            filename = args[0];
            logger.info("Using database file {}", filename);
        } else {
            filename = DEFAULT_DATABASE_NAME;
            logger.info("Using default database file name {}", filename);
        }
        System.setProperty("spring.datasource.url", "jdbc:sqlite:" + filename);

        final File db = new File(filename);
        final String ddlMode;
        if (db.exists()) {
            ddlMode = "none";
        } else {
            ddlMode = "create";
            logger.info("Database file does not exist, creating schema");
        }
        System.setProperty("spring.jpa.hibernate.ddl-auto", ddlMode);

        SpringApplication.run(Accounting.class);
    }

}
