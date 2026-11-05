package com.print.kiosk.service.interfaces;
import com.print.kiosk.dto.request.*;
import com.print.kiosk.dto.response.*;
import com.print.kiosk.service.model.FileDownloadPayload;
public interface RequestProcessingService{
    RequestActionResponse claimNext(Long agentId); ProcessingRequestResponse getDetails(Long requestId,Long agentId);
    FileDownloadPayload downloadFile(Long requestId,Long fileId,Long agentId,String clientIp);
    RequestActionResponse startPrinting(Long requestId,Long agentId); RequestActionResponse complete(Long requestId,Long agentId,CompletePrintingRequest request);
    RequestActionResponse release(Long requestId,Long agentId,ReleaseClaimRequest request); RequestActionResponse pause(Long requestId,Long agentId,PauseRequestRequest request);
    RequestActionResponse reportProblem(Long requestId,Long agentId,ReportProblemRequest request); RequestActionResponse cancel(Long requestId,Long agentId,CancelProcessingRequest request);
    RequestActionResponse reopenByAgent(Long requestId,Long agentId,ReopenRequestRequest request); RequestActionResponse reopenByAdmin(Long requestId,Long adminId,ReopenRequestRequest request);
}