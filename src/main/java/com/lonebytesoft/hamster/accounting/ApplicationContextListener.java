package com.lonebytesoft.hamster.accounting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;

public class ApplicationContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationContextListener.class);

    private static final String SPRING_CONFIG_LOCATION = "spring.xml";
    public static final String ATTRIBUTE_APPLICATION_CONTEXT = "applicationContext";

    private ApplicationContext applicationContext;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Servlet context initializing");

        applicationContext = new ClassPathXmlApplicationContext(SPRING_CONFIG_LOCATION);
        logger.debug("Profiles: {}", Arrays.toString(applicationContext.getEnvironment().getActiveProfiles()));
        sce.getServletContext().setAttribute(ATTRIBUTE_APPLICATION_CONTEXT, applicationContext);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Servlet context destroying");

        deregisterDrivers();
    }

    private void deregisterDrivers() {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            final Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == classLoader) {
                try {
                    logger.debug("Deregistering JDBC driver {}", driver);
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException e) {
                    logger.error("Error deregistering JDBC driver {}", driver, e);
                }
            } else {
                logger.debug("Not deregistering JDBC driver {} as it does not belong to current classloader", driver);
            }
        }
    }

}
