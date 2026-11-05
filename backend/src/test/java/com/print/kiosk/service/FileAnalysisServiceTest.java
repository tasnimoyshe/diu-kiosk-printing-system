package com.print.kiosk.service;

import com.print.kiosk.converter.DocumentConverter;
import com.print.kiosk.entity.PrintFileType;
import com.print.kiosk.exception.FileValidationException;
import com.print.kiosk.service.impl.FileAnalysisServiceImpl;
import com.print.kiosk.service.model.AnalyzedUpload;
import com.print.kiosk.util.PdfPageCounter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileAnalysisServiceTest {

    private final PdfPageCounter pageCounter = new PdfPageCounter();

    @Test
    void validPdfPagesAreCounted() throws Exception {
        FileAnalysisServiceImpl service = service(mock(DocumentConverter.class));
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "two-pages.pdf",
                "application/pdf",
                pdf(2, false)
        );

        AnalyzedUpload result = service.analyze(List.of(file)).get(0);
        assertEquals(PrintFileType.PDF, result.fileType());
        assertEquals(2, result.pageCount());
    }

    @Test
    void passwordProtectedPdfIsRejected() throws Exception {
        FileAnalysisServiceImpl service = service(mock(DocumentConverter.class));
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "locked.pdf",
                "application/pdf",
                pdf(1, true)
        );

        assertThrows(FileValidationException.class, () -> service.analyze(List.of(file)));
    }

    @Test
    void validPngCountsAsOnePage() throws Exception {
        FileAnalysisServiceImpl service = service(mock(DocumentConverter.class));
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "image.png",
                "image/png",
                png()
        );

        AnalyzedUpload result = service.analyze(List.of(file)).get(0);
        assertEquals(PrintFileType.PNG, result.fileType());
        assertEquals(1, result.pageCount());
    }

    @Test
    void unsupportedExtensionIsRejected() {
        FileAnalysisServiceImpl service = service(mock(DocumentConverter.class));
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "notes.txt",
                "text/plain",
                "hello".getBytes(StandardCharsets.UTF_8)
        );

        assertThrows(FileValidationException.class, () -> service.analyze(List.of(file)));
    }

    @Test
    void emptyFileIsRejected() {
        FileAnalysisServiceImpl service = service(mock(DocumentConverter.class));
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        assertThrows(FileValidationException.class, () -> service.analyze(List.of(file)));
    }


    @Test
    void moreThanTenFilesAreRejected() {
        FileAnalysisServiceImpl service = service(mock(DocumentConverter.class));
        List<MultipartFile> files = java.util.stream.IntStream.range(0, 11)
                .mapToObj(index -> (MultipartFile) new MockMultipartFile(
                        "files",
                        "file-" + index + ".pdf",
                        "application/pdf",
                        new byte[]{1}
                ))
                .toList();

        assertThrows(FileValidationException.class, () -> service.analyze(files));
    }

    @Test
    void fileLargerThanTwentyMegabytesIsRejectedBeforeReadingContent() {
        FileAnalysisServiceImpl service = service(mock(DocumentConverter.class));
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(20L * 1024L * 1024L + 1L);
        when(file.getOriginalFilename()).thenReturn("large.pdf");

        assertThrows(FileValidationException.class, () -> service.analyze(List.of(file)));
    }

    @Test
    void totalLargerThanOneHundredMegabytesIsRejected() throws Exception {
        FileAnalysisServiceImpl service = service(mock(DocumentConverter.class));
        byte[] validPdf = pdf(1, false);
        java.util.ArrayList<MultipartFile> files = new java.util.ArrayList<>();

        for (int index = 0; index < 6; index++) {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(18L * 1024L * 1024L);
            when(file.getOriginalFilename()).thenReturn("file-" + index + ".pdf");
            when(file.getBytes()).thenReturn(validPdf);
            files.add(file);
        }

        assertThrows(FileValidationException.class, () -> service.analyze(files));
    }

    @Test
    void corruptedJpegIsRejected() {
        FileAnalysisServiceImpl service = service(mock(DocumentConverter.class));
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "broken.jpg",
                "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x01}
        );

        assertThrows(FileValidationException.class, () -> service.analyze(List.of(file)));
    }

    @Test
    void docxIsConvertedBeforePageCounting() throws Exception {
        DocumentConverter converter = mock(DocumentConverter.class);
        byte[] docx = docx();
        byte[] convertedPdf = pdf(3, false);
        when(converter.convertToPdf("document.docx", docx)).thenReturn(convertedPdf);

        FileAnalysisServiceImpl service = service(converter);
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "document.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                docx
        );

        AnalyzedUpload result = service.analyze(List.of(file)).get(0);
        assertEquals(PrintFileType.DOCX, result.fileType());
        assertEquals(3, result.pageCount());
        verify(converter).convertToPdf("document.docx", docx);
    }

    private FileAnalysisServiceImpl service(DocumentConverter converter) {
        return new FileAnalysisServiceImpl(pageCounter, converter);
    }

    private byte[] pdf(int pages, boolean protectedPdf) throws Exception {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            for (int index = 0; index < pages; index++) {
                document.addPage(new PDPage());
            }
            if (protectedPdf) {
                AccessPermission permission = new AccessPermission();
                StandardProtectionPolicy policy = new StandardProtectionPolicy(
                        "owner-password",
                        "user-password",
                        permission
                );
                policy.setEncryptionKeyLength(128);
                document.protect(policy);
            }
            document.save(output);
            return output.toByteArray();
        }
    }

    private byte[] png() throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", output);
            return output.toByteArray();
        }
    }

    private byte[] docx() throws Exception {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(output)) {
            zip.putNextEntry(new ZipEntry("[Content_Types].xml"));
            zip.write("<Types/>".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();

            zip.putNextEntry(new ZipEntry("word/document.xml"));
            zip.write("<w:document/>".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.finish();
            return output.toByteArray();
        }
    }
}