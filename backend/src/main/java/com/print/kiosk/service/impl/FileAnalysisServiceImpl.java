package com.print.kiosk.service.impl;

import com.print.kiosk.converter.DocumentConverter;
import com.print.kiosk.entity.PrintFileType;
import com.print.kiosk.exception.FileValidationException;
import com.print.kiosk.service.interfaces.FileAnalysisService;
import com.print.kiosk.service.model.AnalyzedUpload;
import com.print.kiosk.util.PdfPageCounter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class FileAnalysisServiceImpl implements FileAnalysisService {

    private static final int MAX_FILES = 10;
    private static final long MAX_FILE_BYTES = 20L * 1024L * 1024L;
    private static final long MAX_TOTAL_BYTES = 100L * 1024L * 1024L;
    private static final long MAX_IMAGE_PIXELS = 50_000_000L;
    private static final int MAX_DOCX_ENTRIES = 10_000;
    private static final long MAX_DOCX_UNCOMPRESSED_BYTES = 200L * 1024L * 1024L;

    private static final byte[] PDF_SIGNATURE = "%PDF-".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] PNG_SIGNATURE = new byte[]{
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };
    private static final byte[] OLE_SIGNATURE = new byte[]{
            (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0,
            (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1
    };

    private final PdfPageCounter pdfPageCounter;
    private final DocumentConverter documentConverter;

    public FileAnalysisServiceImpl(
            PdfPageCounter pdfPageCounter,
            DocumentConverter documentConverter
    ) {
        this.pdfPageCounter = pdfPageCounter;
        this.documentConverter = documentConverter;
    }

    @Override
    public List<AnalyzedUpload> analyze(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new FileValidationException("At least one document is required");
        }
        if (files.size() > MAX_FILES) {
            throw new FileValidationException("A print request may contain at most 10 files");
        }

        long totalSize = 0;
        List<AnalyzedUpload> analyzedUploads = new ArrayList<>(files.size());

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty() || file.getSize() <= 0) {
                throw new FileValidationException("Empty files cannot be submitted");
            }
            if (file.getSize() > MAX_FILE_BYTES) {
                throw new FileValidationException(
                        safeDisplayName(file.getOriginalFilename()) + " exceeds the 20 MB per-file limit"
                );
            }

            totalSize = Math.addExact(totalSize, file.getSize());
            if (totalSize > MAX_TOTAL_BYTES) {
                throw new FileValidationException("The combined upload size must not exceed 100 MB");
            }

            analyzedUploads.add(analyzeSingle(file));
        }

        return List.copyOf(analyzedUploads);
    }

    private AnalyzedUpload analyzeSingle(MultipartFile multipartFile) {
        String originalFileName = safeDisplayName(multipartFile.getOriginalFilename());
        String extension = extensionOf(originalFileName);
        PrintFileType fileType = parseAllowedType(extension);

        byte[] bytes;
        try {
            bytes = multipartFile.getBytes();
        } catch (IOException exception) {
            throw new FileValidationException("Could not read " + originalFileName, exception);
        }

        return switch (fileType) {
            case PDF -> analyzePdf(originalFileName, fileType, bytes);
            case JPG, JPEG -> analyzeJpeg(originalFileName, fileType, bytes);
            case PNG -> analyzePng(originalFileName, bytes);
            case HEIC, HEIF -> analyzeHeif(originalFileName, fileType, bytes);
            case DOC -> analyzeDoc(originalFileName, bytes);
            case DOCX -> analyzeDocx(originalFileName, bytes);
        };
    }

    private AnalyzedUpload analyzePdf(
            String fileName,
            PrintFileType fileType,
            byte[] bytes
    ) {
        requireSignature(bytes, PDF_SIGNATURE, fileName + " does not contain valid PDF content");
        int pages = pdfPageCounter.countPages(bytes, fileName);
        return upload(fileName, fileType, "application/pdf", bytes, pages);
    }

    private AnalyzedUpload analyzeJpeg(
            String fileName,
            PrintFileType fileType,
            byte[] bytes
    ) {
        if (bytes.length < 4
                || (bytes[0] & 0xFF) != 0xFF
                || (bytes[1] & 0xFF) != 0xD8
                || (bytes[2] & 0xFF) != 0xFF) {
            throw new FileValidationException(fileName + " does not contain valid JPEG content");
        }
        validateImage(bytes, fileName, "JPEG");
        return upload(fileName, fileType, "image/jpeg", bytes, 1);
    }

    private AnalyzedUpload analyzePng(String fileName, byte[] bytes) {
        requireSignature(bytes, PNG_SIGNATURE, fileName + " does not contain valid PNG content");
        validateImage(bytes, fileName, "PNG");
        return upload(fileName, PrintFileType.PNG, "image/png", bytes, 1);
    }

    private AnalyzedUpload analyzeHeif(
            String fileName,
            PrintFileType fileType,
            byte[] bytes
    ) {
        validateHeifContainer(bytes, fileName);
        String mimeType = fileType == PrintFileType.HEIC ? "image/heic" : "image/heif";
        return upload(fileName, fileType, mimeType, bytes, 1);
    }

    private AnalyzedUpload analyzeDoc(String fileName, byte[] bytes) {
        requireSignature(bytes, OLE_SIGNATURE, fileName + " does not contain valid DOC content");
        byte[] convertedPdf = documentConverter.convertToPdf(fileName, bytes);
        int pages = pdfPageCounter.countPages(convertedPdf, fileName + " (converted PDF)");
        return upload(fileName, PrintFileType.DOC, "application/msword", bytes, pages);
    }

    private AnalyzedUpload analyzeDocx(String fileName, byte[] bytes) {
        if (startsWith(bytes, OLE_SIGNATURE)) {
            throw new FileValidationException(
                    fileName + " appears to be encrypted or password-protected. Upload an unlocked DOCX file."
            );
        }
        validateDocxPackage(bytes, fileName);
        byte[] convertedPdf = documentConverter.convertToPdf(fileName, bytes);
        int pages = pdfPageCounter.countPages(convertedPdf, fileName + " (converted PDF)");
        return upload(
                fileName,
                PrintFileType.DOCX,
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                bytes,
                pages
        );
    }

    private void validateImage(byte[] bytes, String fileName, String expectedFormat) {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (input == null) {
                throw new FileValidationException(fileName + " is not a readable image");
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new FileValidationException(fileName + " is corrupted or unsupported");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                String formatName = reader.getFormatName();
                if (!formatName.equalsIgnoreCase(expectedFormat)
                        && !(expectedFormat.equals("JPEG") && formatName.equalsIgnoreCase("JPG"))) {
                    throw new FileValidationException(fileName + " content does not match its extension");
                }

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0) {
                    throw new FileValidationException(fileName + " has invalid image dimensions");
                }
                if ((long) width * height > MAX_IMAGE_PIXELS) {
                    throw new FileValidationException(fileName + " is too large to process safely");
                }

                if (reader.read(0) == null) {
                    throw new FileValidationException(fileName + " is corrupted or unreadable");
                }
            } finally {
                reader.dispose();
            }
        } catch (FileValidationException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            throw new FileValidationException(fileName + " is corrupted or unreadable", exception);
        }
    }

    private void validateDocxPackage(byte[] bytes, String fileName) {
        if (bytes.length < 4 || bytes[0] != 'P' || bytes[1] != 'K') {
            throw new FileValidationException(fileName + " does not contain valid DOCX content");
        }

        Set<String> requiredEntries = new HashSet<>();
        int entryCount = 0;
        long totalUncompressed = 0;

        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                entryCount++;
                if (entryCount > MAX_DOCX_ENTRIES) {
                    throw new FileValidationException(fileName + " contains too many document entries");
                }

                String entryName = entry.getName();
                if ("[Content_Types].xml".equals(entryName)
                        || "word/document.xml".equals(entryName)) {
                    requiredEntries.add(entryName);
                }

                byte[] buffer = new byte[8192];
                int read;
                while ((read = zip.read(buffer)) != -1) {
                    totalUncompressed += read;
                    if (totalUncompressed > MAX_DOCX_UNCOMPRESSED_BYTES) {
                        throw new FileValidationException(fileName + " expands beyond the safe DOCX limit");
                    }
                }
                zip.closeEntry();
            }
        } catch (FileValidationException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new FileValidationException(fileName + " is corrupted or is not a valid DOCX file", exception);
        }

        if (!requiredEntries.contains("[Content_Types].xml")
                || !requiredEntries.contains("word/document.xml")) {
            throw new FileValidationException(fileName + " is corrupted or is not a Word DOCX document");
        }
    }

    private void validateHeifContainer(byte[] bytes, String fileName) {
        if (bytes.length < 16 || !ascii(bytes, 4, 4).equals("ftyp")) {
            throw new FileValidationException(fileName + " does not contain valid HEIC/HEIF content");
        }

        Set<String> supportedBrands = Set.of(
                "heic", "heix", "hevc", "hevx", "heim", "heis", "mif1", "msf1"
        );

        boolean supportedBrand = supportedBrands.contains(ascii(bytes, 8, 4));
        for (int offset = 16; offset + 4 <= Math.min(bytes.length, 64); offset += 4) {
            supportedBrand = supportedBrand || supportedBrands.contains(ascii(bytes, offset, 4));
        }
        if (!supportedBrand) {
            throw new FileValidationException(fileName + " is not a supported HEIC/HEIF image");
        }

        int offset = 0;
        boolean foundPayloadBox = false;
        while (offset + 8 <= bytes.length) {
            long boxSize = unsignedInt(bytes, offset);
            String boxType = ascii(bytes, offset + 4, 4);
            int headerSize = 8;

            if (boxSize == 1) {
                if (offset + 16 > bytes.length) {
                    throw new FileValidationException(fileName + " contains a broken HEIC/HEIF box");
                }
                boxSize = ByteBuffer.wrap(bytes, offset + 8, 8)
                        .order(ByteOrder.BIG_ENDIAN)
                        .getLong();
                headerSize = 16;
            } else if (boxSize == 0) {
                boxSize = bytes.length - offset;
            }

            if (boxSize < headerSize || boxSize > bytes.length - offset) {
                throw new FileValidationException(fileName + " is corrupted or truncated");
            }

            if ("meta".equals(boxType) || "mdat".equals(boxType)) {
                foundPayloadBox = true;
            }

            offset += Math.toIntExact(boxSize);
        }

        if (!foundPayloadBox || offset != bytes.length) {
            throw new FileValidationException(fileName + " is corrupted or incomplete");
        }
    }

    private AnalyzedUpload upload(
            String fileName,
            PrintFileType fileType,
            String mimeType,
            byte[] bytes,
            int pageCount
    ) {
        return new AnalyzedUpload(
                fileName,
                fileType,
                mimeType,
                bytes.length,
                pageCount,
                bytes.clone()
        );
    }

    private PrintFileType parseAllowedType(String extension) {
        try {
            return PrintFileType.valueOf(extension.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new FileValidationException(
                    "Unsupported file format. Please upload PDF, DOC, DOCX, JPG, JPEG, PNG, HEIC, or HEIF files."
            );
        }
    }

    private String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 1 || dot == fileName.length() - 1) {
            throw new FileValidationException(
                    "Unsupported file format. Please upload PDF, DOC, DOCX, JPG, JPEG, PNG, HEIC, or HEIF files."
            );
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String safeDisplayName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new FileValidationException("Every uploaded file must have a filename");
        }
        String normalized = originalFileName.replace('\\', '/');
        normalized = normalized.substring(normalized.lastIndexOf('/') + 1);
        normalized = normalized.replaceAll("[\\p{Cntrl}]", "").trim();
        if (normalized.isBlank()) {
            throw new FileValidationException("Every uploaded file must have a valid filename");
        }
        if (normalized.length() > 255) {
            throw new FileValidationException("Filename must not exceed 255 characters");
        }
        return normalized;
    }

    private void requireSignature(byte[] bytes, byte[] signature, String message) {
        if (!startsWith(bytes, signature)) {
            throw new FileValidationException(message);
        }
    }

    private boolean startsWith(byte[] bytes, byte[] signature) {
        if (bytes == null || bytes.length < signature.length) {
            return false;
        }
        for (int index = 0; index < signature.length; index++) {
            if (bytes[index] != signature[index]) {
                return false;
            }
        }
        return true;
    }

    private String ascii(byte[] bytes, int offset, int length) {
        if (offset < 0 || offset + length > bytes.length) {
            return "";
        }
        return new String(bytes, offset, length, StandardCharsets.US_ASCII);
    }

    private long unsignedInt(byte[] bytes, int offset) {
        return Integer.toUnsignedLong(
                ByteBuffer.wrap(bytes, offset, 4)
                        .order(ByteOrder.BIG_ENDIAN)
                        .getInt()
        );
    }
}