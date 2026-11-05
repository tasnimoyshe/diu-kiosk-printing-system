package com.print.kiosk.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name="request_exceptions", indexes={
        @Index(name="idx_request_exceptions_request", columnList="print_request_id, created_at"),
        @Index(name="idx_request_exceptions_unresolved", columnList="resolved_at, created_at")
})
public class RequestException {
    private static final ZoneId DHAKA_ZONE=ZoneId.of("Asia/Dhaka");
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="print_request_id", nullable=false) private PrintRequest printRequest;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="agent_account_id", nullable=false) private UserAccount agent;
    @Enumerated(EnumType.STRING) @Column(name="exception_type", nullable=false, length=40) private RequestExceptionType exceptionType;
    @Column(name="reason", nullable=false, length=500) private String reason;
    @Enumerated(EnumType.STRING) @Column(name="previous_status", nullable=false, length=30) private PrintRequestStatus previousStatus;
    @Enumerated(EnumType.STRING) @Column(name="new_status", nullable=false, length=30) private PrintRequestStatus newStatus;
    @Column(name="created_at", nullable=false, updatable=false) private LocalDateTime createdAt;
    @Column(name="resolved_at") private LocalDateTime resolvedAt;
    @Column(name="resolved_by_account_id") private Long resolvedByAccountId;
    @Column(name="resolution_note", length=500) private String resolutionNote;
    @PrePersist public void beforeInsert(){if(createdAt==null)createdAt=LocalDateTime.now(DHAKA_ZONE);}
    public Long getId(){return id;} public PrintRequest getPrintRequest(){return printRequest;} public void setPrintRequest(PrintRequest v){printRequest=v;}
    public UserAccount getAgent(){return agent;} public void setAgent(UserAccount v){agent=v;}
    public RequestExceptionType getExceptionType(){return exceptionType;} public void setExceptionType(RequestExceptionType v){exceptionType=v;}
    public String getReason(){return reason;} public void setReason(String v){reason=v;}
    public PrintRequestStatus getPreviousStatus(){return previousStatus;} public void setPreviousStatus(PrintRequestStatus v){previousStatus=v;}
    public PrintRequestStatus getNewStatus(){return newStatus;} public void setNewStatus(PrintRequestStatus v){newStatus=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getResolvedAt(){return resolvedAt;} public void setResolvedAt(LocalDateTime v){resolvedAt=v;}
    public Long getResolvedByAccountId(){return resolvedByAccountId;} public void setResolvedByAccountId(Long v){resolvedByAccountId=v;}
    public String getResolutionNote(){return resolutionNote;} public void setResolutionNote(String v){resolutionNote=v;}
}