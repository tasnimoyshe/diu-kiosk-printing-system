package com.print.kiosk.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name="request_claims", indexes={
        @Index(name="idx_request_claims_request_active", columnList="print_request_id, active"),
        @Index(name="idx_request_claims_agent_active", columnList="agent_account_id, active"),
        @Index(name="idx_request_claims_activity", columnList="active, last_activity_at")
})
public class RequestClaim {
    private static final ZoneId DHAKA_ZONE=ZoneId.of("Asia/Dhaka");
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="print_request_id", nullable=false) private PrintRequest printRequest;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="agent_account_id", nullable=false) private UserAccount agent;
    @Column(name="claimed_at", nullable=false, updatable=false) private LocalDateTime claimedAt;
    @Column(name="last_activity_at", nullable=false) private LocalDateTime lastActivityAt;
    @Column(name="released_at") private LocalDateTime releasedAt;
    @Enumerated(EnumType.STRING) @Column(name="release_reason", length=30) private ClaimReleaseReason releaseReason;
    @Column(name="release_note", length=255) private String releaseNote;
    @Column(name="active", nullable=false) private boolean active;
    @Version @Column(name="version", nullable=false) private Long version;
    @PrePersist public void beforeInsert(){LocalDateTime n=LocalDateTime.now(DHAKA_ZONE); if(claimedAt==null)claimedAt=n; if(lastActivityAt==null)lastActivityAt=n; active=true;}
    public Long getId(){return id;} public PrintRequest getPrintRequest(){return printRequest;} public void setPrintRequest(PrintRequest v){printRequest=v;}
    public UserAccount getAgent(){return agent;} public void setAgent(UserAccount v){agent=v;}
    public LocalDateTime getClaimedAt(){return claimedAt;} public LocalDateTime getLastActivityAt(){return lastActivityAt;} public void setLastActivityAt(LocalDateTime v){lastActivityAt=v;}
    public LocalDateTime getReleasedAt(){return releasedAt;} public void setReleasedAt(LocalDateTime v){releasedAt=v;}
    public ClaimReleaseReason getReleaseReason(){return releaseReason;} public void setReleaseReason(ClaimReleaseReason v){releaseReason=v;}
    public String getReleaseNote(){return releaseNote;} public void setReleaseNote(String v){releaseNote=v;}
    public boolean isActive(){return active;} public void setActive(boolean v){active=v;} public Long getVersion(){return version;}
}