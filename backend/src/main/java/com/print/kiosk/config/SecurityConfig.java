package com.print.kiosk.config;

import com.print.kiosk.repository.UserAccountRepository;
import com.print.kiosk.security.AccountAccessPolicy;
import com.print.kiosk.security.AccountStatusFilter;
import com.print.kiosk.security.CookieBearerTokenResolver;
import com.print.kiosk.security.DatabaseUserDetailsService;
import com.print.kiosk.security.RestAccessDeniedHandler;
import com.print.kiosk.security.RestAuthenticationEntryPoint;
import com.print.kiosk.security.SecurityErrorResponseWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(
            DatabaseUserDetailsService databaseUserDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider authenticationProvider =
                new DaoAuthenticationProvider(
                        databaseUserDetailsService
                );

        authenticationProvider.setPasswordEncoder(
                passwordEncoder
        );

        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            DaoAuthenticationProvider authenticationProvider
    ) {
        return new ProviderManager(
                authenticationProvider
        );
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName(
                "role"
        );

        authoritiesConverter.setAuthorityPrefix(
                "ROLE_"
        );

        JwtAuthenticationConverter authenticationConverter =
                new JwtAuthenticationConverter();

        authenticationConverter
                .setJwtGrantedAuthoritiesConverter(
                        authoritiesConverter
                );

        return authenticationConverter;
    }

    /*
     * =========================================================
     * CHAIN 1
     * Public REST API + public WebSocket handshake
     * =========================================================
     */
    @Bean
    @Order(1)
    public SecurityFilterChain publicSecurityFilterChain(
            HttpSecurity http,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler
    ) throws Exception {

        http
                .securityMatcher(
                        "/api/test",

                        "/api/print-requests/**",

                        "/api/auth/login",
                        "/api/auth/refresh",
                        "/api/auth/logout",
                        "/api/auth/csrf",
                        "/api/auth/agents/register",

                        "/ws/public",
                        "/ws/public/**"
                )

                .sessionManagement(
                        session ->
                                session.sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS
                                )
                )

                .csrf(
                        csrf -> csrf
                                .spa()
                                .ignoringRequestMatchers(
                                        "/api/test",
                                        "/api/print-requests/**",
                                        "/api/auth/agents/register"
                                )
                )

                .formLogin(
                        AbstractHttpConfigurer::disable
                )

                .httpBasic(
                        AbstractHttpConfigurer::disable
                )

                .exceptionHandling(
                        exceptionHandling ->
                                exceptionHandling
                                        .authenticationEntryPoint(
                                                authenticationEntryPoint
                                        )
                                        .accessDeniedHandler(
                                                accessDeniedHandler
                                        )
                )

                .authorizeHttpRequests(
                        authorize ->
                                authorize
                                        .anyRequest()
                                        .permitAll()
                );

        return http.build();
    }

    /*
     * =========================================================
     * CHAIN 2
     * Authenticated API + private WebSocket handshake
     * =========================================================
     */
    @Bean
    @Order(2)
    public SecurityFilterChain protectedSecurityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider,
            CookieBearerTokenResolver cookieBearerTokenResolver,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            UserAccountRepository userAccountRepository,
            AccountAccessPolicy accountAccessPolicy,
            SecurityErrorResponseWriter errorResponseWriter,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler
    ) throws Exception {

        AccountStatusFilter accountStatusFilter =
                new AccountStatusFilter(
                        userAccountRepository,
                        accountAccessPolicy,
                        errorResponseWriter
                );

        http
                .securityMatcher(
                        "/api/**",
                        "/ws/private",
                        "/ws/private/**"
                )

                .authenticationProvider(
                        authenticationProvider
                )

                .sessionManagement(
                        session ->
                                session.sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS
                                )
                )

                .csrf(
                        csrf -> csrf.spa()
                )

                .formLogin(
                        AbstractHttpConfigurer::disable
                )

                .httpBasic(
                        AbstractHttpConfigurer::disable
                )

                .exceptionHandling(
                        exceptionHandling ->
                                exceptionHandling
                                        .authenticationEntryPoint(
                                                authenticationEntryPoint
                                        )
                                        .accessDeniedHandler(
                                                accessDeniedHandler
                                        )
                )

                .authorizeHttpRequests(
                        authorize ->
                                authorize

                                        .requestMatchers(
                                                "/api/admin/**"
                                        )
                                        .hasRole(
                                                "ADMIN"
                                        )

                                        .requestMatchers(
                                                "/api/agent/**"
                                        )
                                        .hasRole(
                                                "AGENT"
                                        )

                                        .requestMatchers(
                                                "/api/auth/me",
                                                "/api/auth/password"
                                        )
                                        .authenticated()

                                        .requestMatchers(
                                                "/ws/private",
                                                "/ws/private/**"
                                        )
                                        .authenticated()

                                        .anyRequest()
                                        .denyAll()
                )

                .oauth2ResourceServer(
                        oauth2 ->
                                oauth2
                                        .authenticationEntryPoint(
                                                authenticationEntryPoint
                                        )
                                        .accessDeniedHandler(
                                                accessDeniedHandler
                                        )
                                        .bearerTokenResolver(
                                                cookieBearerTokenResolver
                                        )
                                        .jwt(
                                                jwt ->
                                                        jwt
                                                                .jwtAuthenticationConverter(
                                                                        jwtAuthenticationConverter
                                                                )
                                        )
                )

                .addFilterAfter(
                        accountStatusFilter,
                        BearerTokenAuthenticationFilter.class
                );

        return http.build();
    }

    /*
     * =========================================================
     * CHAIN 3
     * React SPA + static assets
     * =========================================================
     *
     * This chain intentionally does NOT match API requests because
     * /api/** is already handled by the higher-priority chains.
     *
     * It allows:
     *
     * /
     * /queue
     * /login/**
     * /payment/**
     * /admin/**
     * /agent/**
     * /assets/**
     * /index.html
     * favicon files
     *
     * React ProtectedRoute still handles frontend navigation while
     * the backend API remains the authoritative security boundary.
     */
    @Bean
    @Order(3)
    public SecurityFilterChain spaSecurityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .securityMatcher(
                        "/**"
                )

                .sessionManagement(
                        session ->
                                session.sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS
                                )
                )

                .csrf(
                        AbstractHttpConfigurer::disable
                )

                .formLogin(
                        AbstractHttpConfigurer::disable
                )

                .httpBasic(
                        AbstractHttpConfigurer::disable
                )

                .authorizeHttpRequests(
                        authorize ->
                                authorize
                                        .anyRequest()
                                        .permitAll()
                );

        return http.build();
    }
}