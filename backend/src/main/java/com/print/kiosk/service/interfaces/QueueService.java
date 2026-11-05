package com.print.kiosk.service.interfaces;
import com.print.kiosk.dto.response.*;
import com.print.kiosk.entity.*;
import org.springframework.data.domain.*;
public interface QueueService{ Page<QueueEntryResponse> getPublicQueue(String search,Pageable pageable); Page<QueueEntryResponse> getAgentQueue(String search,PaymentStatus paymentStatus,PrintRequestStatus requestStatus,Pageable pageable); Page<ExceptionQueueResponse> getExceptionQueue(Pageable pageable); }