package com.print.kiosk.service.impl;
import com.print.kiosk.dto.response.*;
import com.print.kiosk.entity.*;
import com.print.kiosk.repository.*;
import com.print.kiosk.service.interfaces.QueueService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service
public class QueueServiceImpl implements QueueService{
    private static final List<PrintRequestStatus> ACTIVE=List.of(PrintRequestStatus.CLAIMED,PrintRequestStatus.DOWNLOADING,PrintRequestStatus.PRINTING,PrintRequestStatus.PAID,PrintRequestStatus.AWAITING_PAYMENT);
    private final PrintRequestRepository requests; private final RequestExceptionRepository exceptions;
    public QueueServiceImpl(PrintRequestRepository r,RequestExceptionRepository e){requests=r;exceptions=e;}
    @Transactional(readOnly=true) public Page<QueueEntryResponse> getPublicQueue(String search,Pageable pageable){return mapQueue(requests.findActiveQueue(ACTIVE,norm(search),null,null,pageable),pageable,true);}
    @Transactional(readOnly=true) public Page<QueueEntryResponse> getAgentQueue(String search,PaymentStatus paymentStatus,PrintRequestStatus requestStatus,Pageable pageable){return mapQueue(requests.findActiveQueue(ACTIVE,norm(search),paymentStatus,requestStatus,pageable),pageable,false);}
    @Transactional(readOnly=true) public Page<ExceptionQueueResponse> getExceptionQueue(Pageable pageable){return exceptions.findByResolvedAtIsNullOrderByCreatedAtAsc(pageable).map(e->new ExceptionQueueResponse(e.getId(),e.getPrintRequest().getId(),e.getPrintRequest().getTrackingNumber(),e.getPrintRequest().getFullName(),mask(e.getPrintRequest().getRegistrationNumber()),e.getExceptionType(),e.getReason(),e.getPrintRequest().getStatus(),e.getAgent().getAgentId(),e.getCreatedAt()));}
    private Page<QueueEntryResponse> mapQueue(Page<PrintRequest> page,Pageable pageable,boolean mask){long base=(long)pageable.getPageNumber()*pageable.getPageSize();List<QueueEntryResponse> list=new ArrayList<>();for(int i=0;i<page.getContent().size();i++){PrintRequest r=page.getContent().get(i);UserAccount a=r.getAssignedAgent();list.add(new QueueEntryResponse(base+i+1,r.getId(),r.getTrackingNumber(),r.getFullName(),mask?mask(r.getRegistrationNumber()):r.getRegistrationNumber(),r.getUserType(),r.getPaymentStatus(),r.getStatus(),r.getTotalAmount(),a==null?null:a.getId(),a==null?null:a.getAgentId(),r.getCreatedAt(),r.getClaimedAt()));}return new PageImpl<>(list,pageable,page.getTotalElements());}
    private String norm(String s){return s==null||s.isBlank()?null:s.trim();}
    private String mask(String s){if(s==null||s.length()<4)return "****";return "*".repeat(Math.max(1,s.length()-4))+s.substring(s.length()-4);}
}