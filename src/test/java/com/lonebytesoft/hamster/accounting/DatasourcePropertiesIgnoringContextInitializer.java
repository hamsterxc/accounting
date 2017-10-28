package com.lonebytesoft.hamster.accounting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DatasourcePropertiesIgnoringContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger logger = LoggerFactory.getLogger(DatasourcePropertiesIgnoringContextInitializer.class);

    private static final String DATASOURCE_PROPERTY_PREFIX = "spring.datasource.";

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        final MutablePropertySources propertySources = applicationContext.getEnvironment().getPropertySources();
        final PropertySource<?> systemProperties = propertySources.get(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        if(systemProperties instanceof EnumerablePropertySource) {
            logger.info("Filtering datasource properties in {}", systemProperties);
            propertySources.replace(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME,
                    filterDatasourceProperties((EnumerablePropertySource) systemProperties));
        } else {
            logger.warn("Could not filter datasource properties from {}", systemProperties);
        }
    }

    private PropertySource<?> filterDatasourceProperties(final EnumerablePropertySource<?> propertySource) {
        return new MapPropertySource(
                propertySource.getName(),
                Arrays.stream(propertySource.getPropertyNames())
                        .filter(name -> !name.startsWith(DATASOURCE_PROPERTY_PREFIX))
                        .collect(Collectors.toMap(Function.identity(), propertySource::getProperty))
        );
    }

}
