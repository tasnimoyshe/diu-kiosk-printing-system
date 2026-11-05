package com.print.kiosk.bootstrap;

import com.print.kiosk.entity.PrintingPrice;
import com.print.kiosk.repository.PrintingPriceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PrintingPriceInitializer implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrintingPriceInitializer.class);
    private static final String GLOBAL_CONFIG_KEY = "GLOBAL";

    private final PrintingPriceRepository printingPriceRepository;
    private final BigDecimal colorSingleSided;
    private final BigDecimal blackWhiteSingleSided;
    private final BigDecimal colorDoubleSided;
    private final BigDecimal blackWhiteDoubleSided;

    public PrintingPriceInitializer(
            PrintingPriceRepository printingPriceRepository,
            @Value("${app.pricing.default.color-single-sided}") BigDecimal colorSingleSided,
            @Value("${app.pricing.default.black-white-single-sided}") BigDecimal blackWhiteSingleSided,
            @Value("${app.pricing.default.color-double-sided}") BigDecimal colorDoubleSided,
            @Value("${app.pricing.default.black-white-double-sided}") BigDecimal blackWhiteDoubleSided
    ) {
        this.printingPriceRepository = printingPriceRepository;
        this.colorSingleSided = money(colorSingleSided);
        this.blackWhiteSingleSided = money(blackWhiteSingleSided);
        this.colorDoubleSided = money(colorDoubleSided);
        this.blackWhiteDoubleSided = money(blackWhiteDoubleSided);
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (printingPriceRepository.findByConfigKey(GLOBAL_CONFIG_KEY).isPresent()) {
            LOGGER.info("Global printing price configuration already exists. Initialization skipped.");
            return;
        }

        PrintingPrice price = new PrintingPrice();
        price.setConfigKey(GLOBAL_CONFIG_KEY);
        price.setColorSingleSided(colorSingleSided);
        price.setBlackWhiteSingleSided(blackWhiteSingleSided);
        price.setColorDoubleSided(colorDoubleSided);
        price.setBlackWhiteDoubleSided(blackWhiteDoubleSided);
        price.setRevision(1);
        printingPriceRepository.saveAndFlush(price);

        LOGGER.info("Global printing price configuration created successfully.");
    }

    private BigDecimal money(BigDecimal value) {
        if (value == null || value.signum() <= 0) {
            throw new IllegalStateException("Default printing prices must be greater than zero");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}