package com.print.kiosk.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name="file_download_events", indexes={
        @Index(name="idx_file_download_events_request", columnList="print_request_id, downloaded_at"),
        @Index(name="idx_file_download_events_agent", columnList="agent_account_id, downloaded_at")
})
public class FileDownloadEvent {
    private static final ZoneId DHAKA_ZONE=ZoneId.of("Asia/Dhaka");
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="print_request_id", nullable=false) private PrintRequest printRequest;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="print_request_file_id", nullable=false) private PrintRequestFile printRequestFile;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="agent_account_id", nullable=false) private UserAccount agent;
    @Column(name="downloaded_at", nullable=false, updatable=false) private LocalDateTime downloadedAt;
    @Column(name="byte_count", nullable=false) private long byteCount;
    @Column(name="client_ip", length=64) private String clientIp;
    @PrePersist public void beforeInsert(){if(downloadedAt==null)downloadedAt=LocalDateTime.now(DHAKA_ZONE);}
    public Long getId(){return id;} public PrintRequest getPrintRequest(){return printRequest;} public void setPrintRequest(PrintRequest v){printRequest=v;}
    public PrintRequestFile getPrintRequestFile(){return printRequestFile;} public void setPrintRequestFile(PrintRequestFile v){printRequestFile=v;}
    public UserAccount getAgent(){return agent;} public void setAgent(UserAccount v){agent=v;}
    public LocalDateTime getDownloadedAt(){return downloadedAt;} public long getByteCount(){return byteCount;} public void setByteCount(long v){byteCount=v;}
    public String getClientIp(){return clientIp;} public void setClientIp(String v){clientIp=v;}
}