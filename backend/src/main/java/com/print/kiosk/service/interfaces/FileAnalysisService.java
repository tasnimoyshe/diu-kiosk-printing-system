package com.print.kiosk.service.interfaces;

import com.print.kiosk.service.model.AnalyzedUpload;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileAnalysisService {
    List<AnalyzedUpload> analyze(List<MultipartFile> files);
}