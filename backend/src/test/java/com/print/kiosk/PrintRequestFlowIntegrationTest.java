package com.print.kiosk;

import com.print.kiosk.entity.AccountRole;
import com.print.kiosk.entity.AgentStatus;
import com.print.kiosk.entity.PrintRequest;
import com.print.kiosk.entity.PrintRequestStatus;
import com.print.kiosk.entity.PrintingPrice;
import com.print.kiosk.entity.UserAccount;
import com.print.kiosk.repository.PriceHistoryRepository;
import com.print.kiosk.repository.PrintRequestFileRepository;
import com.print.kiosk.repository.PrintRequestRepository;
import com.print.kiosk.repository.PrintRequestStatusHistoryRepository;
import com.print.kiosk.repository.PrintingPriceRepository;
import com.print.kiosk.repository.RefreshTokenRepository;
import com.print.kiosk.repository.UserAccountRepository;
import com.print.kiosk.service.interfaces.PrintRequestExpirationService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PrintRequestFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PrintRequestRepository printRequestRepository;

    @Autowired
    private PrintRequestFileRepository printRequestFileRepository;

    @Autowired
    private PrintRequestStatusHistoryRepository statusHistoryRepository;

    @Autowired
    private PrintingPriceRepository printingPriceRepository;

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PrintRequestExpirationService expirationService;

    private UserAccount approvedAgent;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        createPricing();
        approvedAgent = userAccountRepository.save(createAgent());
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    @Test
    void submissionStoresPricingSnapshotFilesAndTrackingNumber() throws Exception {
        MvcResult result = submitRequest("COLOR", "DOUBLE_SIDED", 2)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("AWAITING_PAYMENT")))
                .andExpect(jsonPath("$.pricingRevision", is(1)))
                .andExpect(jsonPath("$.totalDocumentPages", is(2)))
                .andExpect(jsonPath("$.totalPhysicalSheets", is(2)))
                .andExpect(jsonPath("$.totalAmount", is(36.0)))
                .andExpect(jsonPath("$.files.length()", is(1)))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertTrue(body.matches("(?s).*PR-\\d{8}-\\d{4}-\\d{3,6}.*"));
        assertEquals(1, printRequestRepository.count());

        PrintRequest request = printRequestRepository.findAll().get(0);
        assertEquals(new BigDecimal("18.00"), request.getAppliedRate());
        assertNotNull(request.getExpiresAt());
        assertTrue(request.getExpiresAt().isAfter(request.getCreatedAt()));
        assertEquals(1, printRequestFileRepository.countByPrintRequest_Id(request.getId()));
        assertEquals(1, statusHistoryRepository.count());
        assertNotNull(printRequestFileRepository.findAll().get(0).getFileData());
    }

    @Test
    void publicCurrentPricingEndpointReturnsGlobalPriceList() throws Exception {
        mockMvc.perform(get("/api/print-requests/pricing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision", is(1)))
                .andExpect(jsonPath("$.colorSingleSided", is(10.0)))
                .andExpect(jsonPath("$.blackWhiteDoubleSided", is(3.5)));
    }

    @Test
    void agentUpdateCreatesHistoryAndExistingRequestKeepsOldRate() throws Exception {
        submitRequest("COLOR", "SINGLE_SIDED", 1)
                .andExpect(status().isCreated());

        mockMvc.perform(
                        patch("/api/agent/pricing")
                                .with(csrf())
                                .with(jwt()
                                        .jwt(jwt -> jwt
                                                .subject(approvedAgent.getAgentId())
                                                .claim("accountId", approvedAgent.getId())
                                                .claim("role", "AGENT")
                                        )
                                        .authorities(new SimpleGrantedAuthority("ROLE_AGENT"))
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "revision": 1,
                                          "colorSingleSided": 12.00,
                                          "blackWhiteSingleSided": 2.00,
                                          "colorDoubleSided": 18.00,
                                          "blackWhiteDoubleSided": 3.50
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision", is(2)))
                .andExpect(jsonPath("$.colorSingleSided", is(12.0)));

        PrintRequest existingRequest = printRequestRepository.findAll().get(0);
        assertEquals(new BigDecimal("10.00"), existingRequest.getAppliedRate());
        assertEquals(1, priceHistoryRepository.count());
    }

    @Test
    void publicCancellationRequiresRegistrationVerificationAndCancelsUnpaidRequest() throws Exception {
        submitRequest("BLACK_AND_WHITE", "SINGLE_SIDED", 1)
                .andExpect(status().isCreated());

        String trackingNumber = printRequestRepository.findAll().get(0).getTrackingNumber();

        mockMvc.perform(
                        post("/api/print-requests/{trackingNumber}/cancel", trackingNumber)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "registrationNumber": "111-11-111",
                                          "reason": "No longer needed"
                                        }
                                        """)
                )
                .andExpect(status().isNotFound());

        mockMvc.perform(
                        post("/api/print-requests/{trackingNumber}/cancel", trackingNumber)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "registrationNumber": "242-15-125",
                                          "reason": "No longer needed"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")))
                .andExpect(jsonPath("$.cancellationReason", is("No longer needed")));

        assertEquals(PrintRequestStatus.CANCELLED, printRequestRepository.findAll().get(0).getStatus());
        assertEquals(2, statusHistoryRepository.count());
    }

    @Test
    void expirationServiceExpiresOverdueUnpaidRequests() throws Exception {
        submitRequest("BLACK_AND_WHITE", "SINGLE_SIDED", 1)
                .andExpect(status().isCreated());

        PrintRequest request = printRequestRepository.findAll().get(0);
        request.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        printRequestRepository.saveAndFlush(request);

        int expired = expirationService.expireDueRequests();

        assertEquals(1, expired);
        assertEquals(PrintRequestStatus.EXPIRED, printRequestRepository.findById(request.getId()).orElseThrow().getStatus());
        assertEquals(2, statusHistoryRepository.count());
    }

    private org.springframework.test.web.servlet.ResultActions submitRequest(
            String color,
            String side,
            int copies
    ) throws Exception {
        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "request.json",
                MediaType.APPLICATION_JSON_VALUE,
                ("""
                        {
                          "fullName": "Test User",
                          "registrationNumber": "242-15-125",
                          "userType": "STUDENT",
                          "department": "CSE",
                          "printColor": "%s",
                          "printingSide": "%s",
                          "copies": %d
                        }
                        """).formatted(color, side, copies).getBytes(StandardCharsets.UTF_8)
        );

        MockMultipartFile file = new MockMultipartFile(
                "files",
                "document.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                pdf(2)
        );

        return mockMvc.perform(
                multipart("/api/print-requests")
                        .file(requestPart)
                        .file(file)
        );
    }

    private byte[] pdf(int pages) throws Exception {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            for (int index = 0; index < pages; index++) {
                document.addPage(new PDPage());
            }
            document.save(output);
            return output.toByteArray();
        }
    }

    private void createPricing() {
        PrintingPrice price = new PrintingPrice();
        price.setConfigKey("GLOBAL");
        price.setColorSingleSided(new BigDecimal("10.00"));
        price.setBlackWhiteSingleSided(new BigDecimal("2.00"));
        price.setColorDoubleSided(new BigDecimal("18.00"));
        price.setBlackWhiteDoubleSided(new BigDecimal("3.50"));
        price.setRevision(1);
        printingPriceRepository.saveAndFlush(price);
    }

    private UserAccount createAgent() {
        UserAccount account = new UserAccount();
        account.setFullName("Pricing Agent");
        account.setAgentId("AGT-PRICE-001");
        account.setEmail("pricing-agent@test.local");
        account.setContactNumber("+8801700000001");
        account.setPasswordHash(passwordEncoder.encode("AgentPass123"));
        account.setRole(AccountRole.AGENT);
        account.setAgentStatus(AgentStatus.APPROVED);
        return account;
    }

    private void cleanDatabase() {
        statusHistoryRepository.deleteAll();
        printRequestFileRepository.deleteAll();
        printRequestRepository.deleteAll();
        priceHistoryRepository.deleteAll();
        printingPriceRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userAccountRepository.deleteAll();
    }
}