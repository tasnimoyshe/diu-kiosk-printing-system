package com.print.kiosk.service.interfaces;
import com.print.kiosk.dto.request.VerifyCashPaymentRequest;
import com.print.kiosk.dto.response.*;
import java.util.List;
public interface CashPaymentService{ CashPinResponse generatePin(Long requestId,Long agentAccountId); PaymentResponse verifyCash(String trackingNumber,VerifyCashPaymentRequest request); List<PaymentOptionResponse> getPaymentOptions(); }