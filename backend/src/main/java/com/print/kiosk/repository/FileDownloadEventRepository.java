package com.print.kiosk.repository;
import com.print.kiosk.entity.FileDownloadEvent;
import org.springframework.data.jpa.repository.JpaRepository;
public interface FileDownloadEventRepository extends JpaRepository<FileDownloadEvent,Long>{ long countByPrintRequest_Id(Long requestId); }