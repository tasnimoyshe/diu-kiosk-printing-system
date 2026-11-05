package com.print.kiosk.service.impl;

import com.print.kiosk.dto.request.UpdatePrintingPriceRequest;
import com.print.kiosk.dto.response.PriceHistoryResponse;
import com.print.kiosk.dto.response.PrintingPriceResponse;
import com.print.kiosk.entity.AccountRole;
import com.print.kiosk.entity.AgentStatus;
import com.print.kiosk.entity.PriceHistory;
import com.print.kiosk.entity.PriceType;
import com.print.kiosk.entity.PrintingPrice;
import com.print.kiosk.entity.UserAccount;
import com.print.kiosk.exception.PricingConfigurationException;
import com.print.kiosk.exception.PricingConflictException;
import com.print.kiosk.exception.ResourceNotFoundException;
import com.print.kiosk.repository.PriceHistoryRepository;
import com.print.kiosk.repository.PrintingPriceRepository;
import com.print.kiosk.repository.UserAccountRepository;
import com.print.kiosk.service.interfaces.PricingService;
import com.print.kiosk.service.model.PricingSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class PricingServiceImpl implements PricingService {

    private static final String GLOBAL_CONFIG_KEY = "GLOBAL";

    private final PrintingPriceRepository printingPriceRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final UserAccountRepository userAccountRepository;

    public PricingServiceImpl(
            PrintingPriceRepository printingPriceRepository,
            PriceHistoryRepository priceHistoryRepository,
            UserAccountRepository userAccountRepository
    ) {
        this.printingPriceRepository = printingPriceRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PrintingPriceResponse getCurrentPricing() {
        return toResponse(getCurrentPrice());
    }

    @Override
    @Transactional(readOnly = true)
    public PricingSnapshot getCurrentSnapshot() {
        PrintingPrice price = getCurrentPrice();
        return new PricingSnapshot(
                price.getId(),
                price.getRevision(),
                price.getColorSingleSided(),
                price.getBlackWhiteSingleSided(),
                price.getColorDoubleSided(),
                price.getBlackWhiteDoubleSided()
        );
    }

    @Override
    @Transactional
    public PrintingPriceResponse updatePricing(
            UpdatePrintingPriceRequest request,
            Long agentAccountId
    ) {
        UserAccount agent = userAccountRepository.findById(agentAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent account was not found"));

        if (agent.getRole() != AccountRole.AGENT || agent.getAgentStatus() != AgentStatus.APPROVED) {
            throw new IllegalArgumentException("Only approved agents may update printing prices");
        }

        PrintingPrice price = printingPriceRepository
                .findByConfigKeyForUpdate(GLOBAL_CONFIG_KEY)
                .orElseThrow(() -> new PricingConfigurationException(
                        "The global printing price configuration is unavailable"
                ));

        if (request.getRevision() == null || request.getRevision() != price.getRevision()) {
            throw new PricingConflictException(
                    "Printing prices changed after this form was loaded. Reload the current prices and try again."
            );
        }

        BigDecimal colorSingle = money(request.getColorSingleSided());
        BigDecimal blackWhiteSingle = money(request.getBlackWhiteSingleSided());
        BigDecimal colorDouble = money(request.getColorDoubleSided());
        BigDecimal blackWhiteDouble = money(request.getBlackWhiteDoubleSided());

        long newRevision = price.getRevision() + 1;
        List<PriceHistory> historyRecords = new ArrayList<>();

        addHistoryIfChanged(
                historyRecords,
                PriceType.COLOR_SINGLE_SIDED,
                price.getColorSingleSided(),
                colorSingle,
                newRevision,
                agent
        );
        addHistoryIfChanged(
                historyRecords,
                PriceType.BLACK_WHITE_SINGLE_SIDED,
                price.getBlackWhiteSingleSided(),
                blackWhiteSingle,
                newRevision,
                agent
        );
        addHistoryIfChanged(
                historyRecords,
                PriceType.COLOR_DOUBLE_SIDED,
                price.getColorDoubleSided(),
                colorDouble,
                newRevision,
                agent
        );
        addHistoryIfChanged(
                historyRecords,
                PriceType.BLACK_WHITE_DOUBLE_SIDED,
                price.getBlackWhiteDoubleSided(),
                blackWhiteDouble,
                newRevision,
                agent
        );

        if (historyRecords.isEmpty()) {
            return toResponse(price);
        }

        price.setColorSingleSided(colorSingle);
        price.setBlackWhiteSingleSided(blackWhiteSingle);
        price.setColorDoubleSided(colorDouble);
        price.setBlackWhiteDoubleSided(blackWhiteDouble);
        price.setRevision(newRevision);
        price.setUpdatedByAccountId(agent.getId());
        price.setUpdatedByAgentId(agent.getAgentId());

        PrintingPrice savedPrice = printingPriceRepository.saveAndFlush(price);
        priceHistoryRepository.saveAll(historyRecords);

        return toResponse(savedPrice);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PriceHistoryResponse> getPriceHistory(Pageable pageable) {
        return priceHistoryRepository
                .findAllByOrderByChangedAtDesc(pageable)
                .map(this::toHistoryResponse);
    }

    private PrintingPrice getCurrentPrice() {
        return printingPriceRepository.findByConfigKey(GLOBAL_CONFIG_KEY)
                .orElseThrow(() -> new PricingConfigurationException(
                        "The global printing price configuration is unavailable"
                ));
    }

    private void addHistoryIfChanged(
            List<PriceHistory> historyRecords,
            PriceType priceType,
            BigDecimal previousPrice,
            BigDecimal newPrice,
            long newRevision,
            UserAccount agent
    ) {
        if (money(previousPrice).compareTo(newPrice) == 0) {
            return;
        }

        PriceHistory history = new PriceHistory();
        history.setPriceType(priceType);
        history.setPreviousPrice(money(previousPrice));
        history.setNewPrice(newPrice);
        history.setPricingRevision(newRevision);
        history.setChangedByAccountId(agent.getId());
        history.setChangedByAgentId(agent.getAgentId());
        historyRecords.add(history);
    }

    private BigDecimal money(BigDecimal value) {
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException("Every printing price must be greater than zero");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private PrintingPriceResponse toResponse(PrintingPrice price) {
        return new PrintingPriceResponse(
                price.getId(),
                price.getRevision(),
                price.getColorSingleSided(),
                price.getBlackWhiteSingleSided(),
                price.getColorDoubleSided(),
                price.getBlackWhiteDoubleSided(),
                price.getUpdatedByAgentId(),
                price.getUpdatedAt()
        );
    }

    private PriceHistoryResponse toHistoryResponse(PriceHistory history) {
        return new PriceHistoryResponse(
                history.getId(),
                history.getPriceType(),
                history.getPreviousPrice(),
                history.getNewPrice(),
                history.getPricingRevision(),
                history.getChangedByAccountId(),
                history.getChangedByAgentId(),
                history.getChangedAt()
        );
    }
}