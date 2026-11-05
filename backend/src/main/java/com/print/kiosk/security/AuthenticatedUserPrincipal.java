package com.print.kiosk.security;

import com.print.kiosk.entity.AccountRole;
import com.print.kiosk.entity.AgentStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AuthenticatedUserPrincipal implements UserDetails {

    private final Long accountId;
    private final String fullName;
    private final String agentId;
    private final String email;
    private final String passwordHash;
    private final AccountRole role;
    private final AgentStatus agentStatus;

    public AuthenticatedUserPrincipal(
            Long accountId,
            String fullName,
            String agentId,
            String email,
            String passwordHash,
            AccountRole role,
            AgentStatus agentStatus
    ) {
        this.accountId = accountId;
        this.fullName = fullName;
        this.agentId = agentId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.agentStatus = agentStatus;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + role.name())
        );
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        if (role == AccountRole.ADMIN) {
            return true;
        }

        return role == AccountRole.AGENT
                && agentStatus == AgentStatus.APPROVED;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getEmail() {
        return email;
    }

    public AccountRole getRole() {
        return role;
    }

    public AgentStatus getAgentStatus() {
        return agentStatus;
    }
}