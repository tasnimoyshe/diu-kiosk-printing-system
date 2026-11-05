package com.print.kiosk;

import com.print.kiosk.entity.AccountRole;
import com.print.kiosk.entity.AgentStatus;
import com.print.kiosk.entity.UserAccount;
import com.print.kiosk.repository.RefreshTokenRepository;
import com.print.kiosk.repository.UserAccountRepository;
import com.print.kiosk.security.AuthCookieService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationFlowIntegrationTest {

    private static final String ADMIN_EMAIL =
            "admin@kiosk.test";

    private static final String ADMIN_PASSWORD =
            "AdminPass123";

    private static final String AGENT_ID =
            "AGT-TEST-001";

    private static final String AGENT_PASSWORD =
            "AgentPass123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userAccountRepository.deleteAll();

        userAccountRepository.save(
                createAdmin()
        );

        userAccountRepository.save(
                createAgent(
                        AgentStatus.APPROVED
                )
        );
    }

    @Test
    void loginCreatesPersistentHttpOnlyCookiesAndMeWorks()
            throws Exception {
        MvcResult loginResult = login(
                ADMIN_EMAIL,
                ADMIN_PASSWORD
        );

        Cookie accessCookie =
                loginResult
                        .getResponse()
                        .getCookie(
                                AuthCookieService
                                        .ACCESS_TOKEN_COOKIE
                        );

        Cookie refreshCookie =
                loginResult
                        .getResponse()
                        .getCookie(
                                AuthCookieService
                                        .REFRESH_TOKEN_COOKIE
                        );

        assertNotNull(accessCookie);
        assertNotNull(refreshCookie);

        assertTrue(
                accessCookie.isHttpOnly()
        );

        assertTrue(
                refreshCookie.isHttpOnly()
        );

        assertTrue(
                refreshCookie.getMaxAge() > 0
        );

        mockMvc.perform(
                        get("/api/auth/me")
                                .cookie(
                                        accessCookie
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath(
                                "$.email",
                                is(ADMIN_EMAIL)
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.role",
                                is("ADMIN")
                        )
                );
    }

    @Test
    void refreshRotatesTokenAndOldTokenCannotBeReused()
            throws Exception {
        MvcResult loginResult = login(
                ADMIN_EMAIL,
                ADMIN_PASSWORD
        );

        Cookie oldRefreshCookie =
                loginResult
                        .getResponse()
                        .getCookie(
                                AuthCookieService
                                        .REFRESH_TOKEN_COOKIE
                        );

        assertNotNull(oldRefreshCookie);

        MvcResult refreshResult =
                mockMvc.perform(
                                post("/api/auth/refresh")
                                        .with(csrf())
                                        .cookie(
                                                oldRefreshCookie
                                        )
                        )
                        .andExpect(
                                status().isOk()
                        )
                        .andExpect(
                                cookie().exists(
                                        AuthCookieService
                                                .ACCESS_TOKEN_COOKIE
                                )
                        )
                        .andExpect(
                                cookie().exists(
                                        AuthCookieService
                                                .REFRESH_TOKEN_COOKIE
                                )
                        )
                        .andReturn();

        Cookie newRefreshCookie =
                refreshResult
                        .getResponse()
                        .getCookie(
                                AuthCookieService
                                        .REFRESH_TOKEN_COOKIE
                        );

        assertNotNull(newRefreshCookie);

        assertNotEquals(
                oldRefreshCookie.getValue(),
                newRefreshCookie.getValue()
        );

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .with(csrf())
                                .cookie(
                                        oldRefreshCookie
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                )
                .andExpect(
                        jsonPath(
                                "$.message",
                                containsString(
                                        "used or revoked"
                                )
                        )
                );
    }

    @Test
    void logoutRevokesRefreshTokenAndClearsCookies()
            throws Exception {
        MvcResult loginResult = login(
                ADMIN_EMAIL,
                ADMIN_PASSWORD
        );

        Cookie refreshCookie =
                loginResult
                        .getResponse()
                        .getCookie(
                                AuthCookieService
                                        .REFRESH_TOKEN_COOKIE
                        );

        assertNotNull(refreshCookie);

        mockMvc.perform(
                        post("/api/auth/logout")
                                .with(csrf())
                                .cookie(
                                        refreshCookie
                                )
                )
                .andExpect(
                        status().isNoContent()
                )
                .andExpect(
                        cookie().maxAge(
                                AuthCookieService
                                        .ACCESS_TOKEN_COOKIE,
                                0
                        )
                )
                .andExpect(
                        cookie().maxAge(
                                AuthCookieService
                                        .REFRESH_TOKEN_COOKIE,
                                0
                        )
                );

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .with(csrf())
                                .cookie(
                                        refreshCookie
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void agentCannotAccessAdminEndpoint()
            throws Exception {
        MvcResult loginResult = login(
                AGENT_ID,
                AGENT_PASSWORD
        );

        Cookie accessCookie =
                loginResult
                        .getResponse()
                        .getCookie(
                                AuthCookieService
                                        .ACCESS_TOKEN_COOKIE
                        );

        assertNotNull(accessCookie);

        mockMvc.perform(
                        get(
                                "/api/admin/agents/pending"
                        )
                                .cookie(
                                        accessCookie
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }

    @Test
    void adminCanAccessAdminEndpoint()
            throws Exception {
        MvcResult loginResult = login(
                ADMIN_EMAIL,
                ADMIN_PASSWORD
        );

        Cookie accessCookie =
                loginResult
                        .getResponse()
                        .getCookie(
                                AuthCookieService
                                        .ACCESS_TOKEN_COOKIE
                        );

        assertNotNull(accessCookie);

        mockMvc.perform(
                        get(
                                "/api/admin/agents/pending"
                        )
                                .cookie(
                                        accessCookie
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    void everyUnavailableAgentStatusIsBlocked()
            throws Exception {
        for (
                AgentStatus status
                : new AgentStatus[]{
                AgentStatus.PENDING,
                AgentStatus.REJECTED,
                AgentStatus.SUSPENDED,
                AgentStatus.DELETED
        }
        ) {
            refreshTokenRepository.deleteAll();
            userAccountRepository.deleteAll();

            UserAccount blockedAgent =
                    createAgent(status);

            userAccountRepository.save(
                    blockedAgent
            );

            mockMvc.perform(
                            post("/api/auth/login")
                                    .with(csrf())
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(
                                            loginJson(
                                                    AGENT_ID,
                                                    AGENT_PASSWORD
                                            )
                                    )
                    )
                    .andExpect(
                            status().isForbidden()
                    );
        }
    }

    @Test
    void refreshTokenIsStoredOnlyAsSha256Hash()
            throws Exception {
        MvcResult loginResult = login(
                ADMIN_EMAIL,
                ADMIN_PASSWORD
        );

        Cookie refreshCookie =
                loginResult
                        .getResponse()
                        .getCookie(
                                AuthCookieService
                                        .REFRESH_TOKEN_COOKIE
                        );

        assertNotNull(refreshCookie);

        assertEquals(
                1L,
                refreshTokenRepository.count()
        );

        String storedHash =
                refreshTokenRepository
                        .findAll()
                        .get(0)
                        .getTokenHash();

        assertEquals(
                64,
                storedHash.length()
        );

        assertNotEquals(
                refreshCookie.getValue(),
                storedHash
        );
    }

    private MvcResult login(
            String loginId,
            String password
    ) throws Exception {
        return mockMvc.perform(
                        post("/api/auth/login")
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        loginJson(
                                                loginId,
                                                password
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        cookie().httpOnly(
                                AuthCookieService
                                        .ACCESS_TOKEN_COOKIE,
                                true
                        )
                )
                .andExpect(
                        cookie().httpOnly(
                                AuthCookieService
                                        .REFRESH_TOKEN_COOKIE,
                                true
                        )
                )
                .andReturn();
    }

    private String loginJson(
            String loginId,
            String password
    ) {
        return """
                {
                  "loginId": "%s",
                  "password": "%s"
                }
                """.formatted(
                loginId,
                password
        );
    }

    private UserAccount createAdmin() {
        UserAccount account =
                new UserAccount();

        account.setFullName(
                "Integration Admin"
        );

        account.setEmail(
                ADMIN_EMAIL
        );

        account.setPasswordHash(
                passwordEncoder.encode(
                        ADMIN_PASSWORD
                )
        );

        account.setRole(
                AccountRole.ADMIN
        );

        account.setAgentId(null);
        account.setContactNumber(null);
        account.setAgentStatus(null);

        return account;
    }

    private UserAccount createAgent(
            AgentStatus status
    ) {
        UserAccount account =
                new UserAccount();

        account.setFullName(
                "Integration Agent"
        );

        account.setAgentId(
                AGENT_ID
        );

        account.setEmail(
                "agent@kiosk.test"
        );

        account.setContactNumber(
                "01700000000"
        );

        account.setPasswordHash(
                passwordEncoder.encode(
                        AGENT_PASSWORD
                )
        );

        account.setRole(
                AccountRole.AGENT
        );

        account.setAgentStatus(
                status
        );

        return account;
    }
}