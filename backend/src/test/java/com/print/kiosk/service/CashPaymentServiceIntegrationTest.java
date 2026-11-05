package com.print.kiosk.service;

import com.print.kiosk.dto.request.VerifyCashPaymentRequest;
import com.print.kiosk.dto.response.CashPinResponse;
import com.print.kiosk.entity.*;
import com.print.kiosk.exception.*;
import com.print.kiosk.repository.*;
import com.print.kiosk.service.interfaces.CashPaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CashPaymentServiceIntegrationTest {
    @Autowired private CashPaymentService service;
    @Autowired private UserAccountRepository accounts;
    @Autowired private PrintRequestRepository requests;
    @Autowired private CashPaymentPinRepository pins;

    @Test
    void generatedPinIsFourDigitsAndOnlyHashIsStored() {
        UserAccount agent = agent("AGT-PIN-1", "pin1@test.local");
        PrintRequest request = request("PR-PIN-1");
        CashPinResponse response = service.generatePin(request.getId(), agent.getId());
        assertTrue(response.pin().matches("\\d{4}"));
        CashPaymentPin stored = pins.findAll().get(0);
        assertEquals(64, stored.getPinHash().length());
        assertNotEquals(response.pin(), stored.getPinHash());
    }

    @Test
    void correctPinMarksPaymentAndRequestPaid() {
        UserAccount agent = agent("AGT-PIN-2", "pin2@test.local");
        PrintRequest request = request("PR-PIN-2");
        CashPinResponse response = service.generatePin(request.getId(), agent.getId());
        VerifyCashPaymentRequest verification = verify(response.pin());
        service.verifyCash(request.getTrackingNumber(), verification);
        PrintRequest saved = requests.findById(request.getId()).orElseThrow();
        assertEquals(PaymentStatus.PAID, saved.getPaymentStatus());
        assertEquals(PrintRequestStatus.PAID, saved.getStatus());
        assertNotNull(pins.findAll().get(0).getUsedAt());
    }

    @Test
    void fiveWrongAttemptsBlockVerification() {
        UserAccount agent = agent("AGT-PIN-3", "pin3@test.local");
        PrintRequest request = request("PR-PIN-3");
        service.generatePin(request.getId(), agent.getId());
        for (int attempt = 1; attempt < 5; attempt++) {
            assertThrows(InvalidPinException.class,
                    () -> service.verifyCash(request.getTrackingNumber(), verify("9999")));
        }
        assertThrows(PinVerificationBlockedException.class,
                () -> service.verifyCash(request.getTrackingNumber(), verify("9999")));
        assertNotNull(pins.findAll().get(0).getBlockedUntil());
    }

    @Test
    void expiredPinCannotBeUsed() {
        UserAccount agent = agent("AGT-PIN-4", "pin4@test.local");
        PrintRequest request = request("PR-PIN-4");
        CashPinResponse response = service.generatePin(request.getId(), agent.getId());
        CashPaymentPin pin = pins.findAll().get(0);
        pin.setExpiresAt(LocalDateTime.now().minusSeconds(1));
        pins.saveAndFlush(pin);
        assertThrows(ExpiredPinException.class,
                () -> service.verifyCash(request.getTrackingNumber(), verify(response.pin())));
    }

    private VerifyCashPaymentRequest verify(String pin) {
        VerifyCashPaymentRequest request = new VerifyCashPaymentRequest();
        request.setRegistrationNumber("242-15-125");
        request.setPin(pin);
        return request;
    }

    private UserAccount agent(String agentId, String email) {
        UserAccount account = new UserAccount();
        account.setFullName("Payment Agent");
        account.setAgentId(agentId);
        account.setEmail(email);
        account.setContactNumber("017" + Math.abs(agentId.hashCode() % 100000000));
        account.setPasswordHash("hash");
        account.setRole(AccountRole.AGENT);
        account.setAgentStatus(AgentStatus.APPROVED);
        return accounts.saveAndFlush(account);
    }

    private PrintRequest request(String tracking) {
        PrintRequest request = new PrintRequest();
        request.setTrackingNumber(tracking);
        request.setFullName("Payment User");
        request.setRegistrationNumber("242-15-125");
        request.setUserType(UserType.STUDENT);
        request.setPrintColor(PrintColor.COLOR);
        request.setPrintingSide(PrintingSide.SINGLE_SIDED);
        request.setCopies(1);
        request.setStatus(PrintRequestStatus.AWAITING_PAYMENT);
        request.setPaymentStatus(PaymentStatus.PENDING);
        request.setPricingConfigId(1L);
        request.setPricingRevision(1);
        request.setAppliedRate(new BigDecimal("10.00"));
        request.setTotalDocumentPages(1);
        request.setTotalPhysicalSheets(1);
        request.setTotalAmount(new BigDecimal("10.00"));
        request.setCalculatedAt(LocalDateTime.now());
        request.setExpiresAt(LocalDateTime.now().plusHours(12));
        return requests.saveAndFlush(request);
    }
}