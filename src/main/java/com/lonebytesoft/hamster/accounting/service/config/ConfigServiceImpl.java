package com.lonebytesoft.hamster.accounting.service.config;

import com.lonebytesoft.hamster.accounting.model.Config;
import com.lonebytesoft.hamster.accounting.model.ConfigEntry;
import com.lonebytesoft.hamster.accounting.model.Currency;
import com.lonebytesoft.hamster.accounting.repository.ConfigEntryRepository;
import com.lonebytesoft.hamster.accounting.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConfigServiceImpl implements ConfigService {

    private final Map<String, ConfigEntryMapper> mappers;

    private final ConfigEntryRepository configEntryRepository;
    private final CurrencyRepository currencyRepository;

    @Autowired
    public ConfigServiceImpl(final ConfigEntryRepository configEntryRepository, final CurrencyRepository currencyRepository) {
        this.configEntryRepository = configEntryRepository;
        this.currencyRepository = currencyRepository;

        mappers = new HashMap<>();
        mappers.put("currency_id_default", new ConfigEntryMapper(
                (config, value) -> {
                    final long currencyId = Long.parseLong(value);
                    final Currency currency = currencyRepository.findById(currencyId)
                            .orElseThrow(() -> new IllegalStateException("No default currency found, id=" + currencyId));
                    config.setCurrencyDefault(currency);
                },
                config -> String.valueOf(config.getCurrencyDefault().getId())
        ));
    }

    @Override
    public Config get() {
        final Config config = new Config();
        configEntryRepository.findAll().forEach(configEntry -> {
            mappers.computeIfPresent(configEntry.getKey(), (key, mapper) -> {
                mapper.getRead().accept(config, configEntry.getValue());
                return mapper;
            });
        });
        return config;
    }

    @Override
    public void save(Config config) {
        configEntryRepository.deleteAll();
        configEntryRepository.saveAll(
                mappers
                        .entrySet()
                        .stream()
                        .map(entry -> new ConfigEntry(
                                entry.getKey(),
                                entry.getValue().getWrite().apply(config)
                        ))
                        .collect(Collectors.toList())
        );
    }

}
