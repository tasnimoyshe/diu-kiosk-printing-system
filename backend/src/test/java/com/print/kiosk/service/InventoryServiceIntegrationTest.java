package com.print.kiosk.service;

import com.print.kiosk.dto.request.ChangeInventoryItemPriceRequest;
import com.print.kiosk.dto.request.CreateInventoryCategoryRequest;
import com.print.kiosk.dto.request.CreateInventoryItemRequest;
import com.print.kiosk.dto.request.InventoryReasonRequest;
import com.print.kiosk.dto.request.ManualInventoryCorrectionRequest;
import com.print.kiosk.dto.request.MergeInventoryCategoryRequest;
import com.print.kiosk.dto.response.InventoryCategoryResponse;
import com.print.kiosk.dto.response.InventoryItemResponse;
import com.print.kiosk.dto.response.InventoryTransactionResponse;
import com.print.kiosk.entity.AccountRole;
import com.print.kiosk.entity.AgentStatus;
import com.print.kiosk.entity.InventoryTransaction;
import com.print.kiosk.entity.InventoryTransactionType;
import com.print.kiosk.entity.UserAccount;
import com.print.kiosk.exception.InventoryConflictException;
import com.print.kiosk.repository.InventoryItemRepository;
import com.print.kiosk.repository.InventoryTransactionRepository;
import com.print.kiosk.repository.UserAccountRepository;
import com.print.kiosk.service.interfaces.InventoryCategoryService;
import com.print.kiosk.service.interfaces.InventoryItemService;
import com.print.kiosk.service.interfaces.InventoryTransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InventoryServiceIntegrationTest {

    @Autowired
    private InventoryCategoryService categoryService;

    @Autowired
    private InventoryItemService itemService;

    @Autowired
    private InventoryTransactionService transactionService;

    @Autowired
    private InventoryTransactionRepository transactionRepository;

    @Autowired
    private InventoryItemRepository itemRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Test
    void saleReturnAndCorrectionNeverAllowNegativeNetQuantity() {
        UserAccount agent = agent();
        InventoryItemResponse item = item(agent, "Beverage", "Frutika", "30.00");

        InventoryTransactionResponse sale = transactionService.recordSale(
                item.id(),
                agent.getId()
        );
        assertEquals(1, sale.quantity());
        assertEquals(new BigDecimal("30.00"), sale.totalAmount());

        InventoryReasonRequest returnRequest = new InventoryReasonRequest();
        returnRequest.setReason("Customer returned the product");
        InventoryTransactionResponse returned = transactionService.recordReturn(
                item.id(),
                agent.getId(),
                returnRequest
        );
        assertEquals(-1, returned.quantity());
        assertEquals(new BigDecimal("-30.00"), returned.totalAmount());

        assertThrows(
                InventoryConflictException.class,
                () -> transactionService.recordReturn(
                        item.id(),
                        agent.getId(),
                        returnRequest
                )
        );

        ManualInventoryCorrectionRequest correction =
                new ManualInventoryCorrectionRequest();
        correction.setQuantity(1);
        correction.setReason("Recovered one missing sales entry");
        transactionService.recordCorrection(
                item.id(),
                agent.getId(),
                correction
        );

        InventoryItemResponse current = itemService.get(item.id());
        assertEquals(1, current.netSoldQuantity());
        assertEquals(new BigDecimal("30.00"), current.netRevenue());
    }

    @Test
    void priceChangesAffectOnlyFutureTransactions() {
        UserAccount agent = agent();
        InventoryItemResponse item = item(agent, "Snack", "Chips", "20.00");

        transactionService.recordSale(item.id(), agent.getId());

        ChangeInventoryItemPriceRequest priceRequest =
                new ChangeInventoryItemPriceRequest();
        priceRequest.setPrice(new BigDecimal("25.00"));
        itemService.changePrice(item.id(), agent.getId(), priceRequest);

        transactionService.recordSale(item.id(), agent.getId());

        List<InventoryTransaction> transactions = transactionRepository.findAll()
                .stream()
                .filter(transaction -> transaction.getItem().getId().equals(item.id()))
                .toList();

        assertEquals(2, transactions.size());
        assertTrue(transactions.stream().anyMatch(transaction ->
                transaction.getUnitPrice().compareTo(new BigDecimal("20.00")) == 0
        ));
        assertTrue(transactions.stream().anyMatch(transaction ->
                transaction.getUnitPrice().compareTo(new BigDecimal("25.00")) == 0
        ));
        assertEquals(1, itemService.priceHistory(item.id()).size());
    }

    @Test
    void categoryMergeMovesItemsAndPreservesTransactionSnapshots() {
        UserAccount agent = agent();
        InventoryCategoryResponse source = category(agent, "Soft Drinks");
        InventoryCategoryResponse destination = category(agent, "Beverage");
        InventoryItemResponse item = itemInCategory(
                agent,
                source.id(),
                "Cola",
                "40.00"
        );

        transactionService.recordSale(item.id(), agent.getId());

        MergeInventoryCategoryRequest mergeRequest =
                new MergeInventoryCategoryRequest();
        mergeRequest.setDestinationCategoryId(destination.id());
        categoryService.merge(source.id(), agent.getId(), mergeRequest);

        assertEquals(
                destination.id(),
                itemRepository.findDetailedById(item.id()).orElseThrow()
                        .getCategory().getId()
        );

        InventoryTransaction transaction = transactionRepository.findAll()
                .stream()
                .filter(value -> value.getItem().getId().equals(item.id()))
                .findFirst()
                .orElseThrow();
        assertEquals(source.name(), transaction.getCategoryNameSnapshot());
        assertEquals(source.id(), transaction.getCategoryIdSnapshot());
    }

    @Test
    void archiveAndDeleteRulesAreEnforced() {
        UserAccount agent = agent();
        InventoryCategoryResponse category = category(agent, "Stationery");
        InventoryItemResponse item = itemInCategory(
                agent,
                category.id(),
                "Pen",
                "10.00"
        );

        categoryService.archive(category.id(), agent.getId());
        assertThrows(
                InventoryConflictException.class,
                () -> transactionService.recordSale(item.id(), agent.getId())
        );

        categoryService.reactivate(category.id(), agent.getId());
        itemService.reactivate(item.id(), agent.getId());
        transactionService.recordSale(item.id(), agent.getId());

        assertThrows(
                InventoryConflictException.class,
                () -> categoryService.delete(category.id(), agent.getId())
        );

        itemService.archive(item.id(), agent.getId());
        InventoryCategoryResponse deleted = categoryService.delete(
                category.id(),
                agent.getId()
        );
        assertTrue(deleted.deleted());
        assertTrue(deleted.archived());
    }

    private UserAccount agent() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        UserAccount account = new UserAccount();
        account.setFullName("Inventory Agent");
        account.setAgentId("AGT-INV-" + suffix);
        account.setEmail("inventory-" + suffix + "@test.local");
        account.setContactNumber("017" + Math.abs(suffix.hashCode() % 100000000));
        account.setPasswordHash("test-password-hash");
        account.setRole(AccountRole.AGENT);
        account.setAgentStatus(AgentStatus.APPROVED);
        return userAccountRepository.saveAndFlush(account);
    }

    private InventoryCategoryResponse category(
            UserAccount agent,
            String name
    ) {
        CreateInventoryCategoryRequest request =
                new CreateInventoryCategoryRequest();
        request.setName(name + " " + UUID.randomUUID().toString().substring(0, 5));
        InventoryCategoryResponse created = categoryService.create(
                agent.getId(),
                request
        );
        assertNotNull(created.id());
        return created;
    }

    private InventoryItemResponse item(
            UserAccount agent,
            String categoryName,
            String itemName,
            String price
    ) {
        CreateInventoryItemRequest request = new CreateInventoryItemRequest();
        request.setName(itemName + " " + UUID.randomUUID().toString().substring(0, 5));
        request.setCategoryName(categoryName + " " + UUID.randomUUID().toString().substring(0, 5));
        request.setPrice(new BigDecimal(price));
        return itemService.create(agent.getId(), request);
    }

    private InventoryItemResponse itemInCategory(
            UserAccount agent,
            Long categoryId,
            String itemName,
            String price
    ) {
        CreateInventoryItemRequest request = new CreateInventoryItemRequest();
        request.setName(itemName + " " + UUID.randomUUID().toString().substring(0, 5));
        request.setCategoryId(categoryId);
        request.setPrice(new BigDecimal(price));
        return itemService.create(agent.getId(), request);
    }
}