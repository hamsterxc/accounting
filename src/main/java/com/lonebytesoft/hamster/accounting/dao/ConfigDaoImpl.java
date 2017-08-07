package com.lonebytesoft.hamster.accounting.dao;

import com.lonebytesoft.hamster.accounting.model.Config;
import com.lonebytesoft.hamster.accounting.model.ConfigEntry;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

public class ConfigDaoImpl extends AbstractDao<ConfigEntry> implements ConfigDao {

    private static final Logger logger = LoggerFactory.getLogger(ConfigDaoImpl.class);

    private static final String KEY_CURRENCY_ID_DEFAULT = "currency_id_default";

    public ConfigDaoImpl(final SessionFactory sessionFactory) {
        super(sessionFactory, ConfigEntry.class);
    }

    @Override
    public Config get() {
        return query((criteriaBuilder, criteriaQuery, root) -> {
        }, query -> {
            final Collection<ConfigEntry> entries = query.list();
            logger.debug("Fetched {} config entries", entries.size());

            final Map<String, ConfigEntry> map = obtainMapById(entries);
            final Config config = new Config();

            config.setCurrencyIdDefault(Long.parseLong(map.get(KEY_CURRENCY_ID_DEFAULT).getValue()));

            return config;
        });
    }

    @Override
    public void save(Config config) {
        logger.debug("Saving {}", config);
        executeInTransaction(session -> {
            saveEntry(session, KEY_CURRENCY_ID_DEFAULT, config.getCurrencyIdDefault());
        });
    }

    private void saveEntry(final Session session, final String key, final Object value) {
        final ConfigEntry entry = new ConfigEntry();
        entry.setKey(key);
        entry.setValue(String.valueOf(value));

        session.saveOrUpdate(entry);
    }

}
