package com.print.kiosk.converter;

import com.print.kiosk.exception.DocumentConversionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Comparator;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Component
public class LibreOfficeDocumentConverter implements DocumentConverter {

    private final String sofficePath;
    private final Duration timeout;

    public LibreOfficeDocumentConverter(
            @Value("${app.document-converter.soffice-path}") String sofficePath,
            @Value("${app.document-converter.timeout-seconds:60}") long timeoutSeconds
    ) {
        if (sofficePath == null || sofficePath.isBlank()) {
            throw new IllegalStateException("LibreOffice soffice path is required");
        }
        if (timeoutSeconds <= 0) {
            throw new IllegalStateException("Document conversion timeout must be greater than zero");
        }
        this.sofficePath = sofficePath.trim();
        this.timeout = Duration.ofSeconds(timeoutSeconds);
    }

    @Override
    public byte[] convertToPdf(String originalFileName, byte[] sourceBytes) {
        Path temporaryDirectory = null;

        try {
            temporaryDirectory = Files.createTempDirectory("kiosk-convert-");
            String safeName = safeFileName(originalFileName);
            Path inputFile = temporaryDirectory.resolve(UUID.randomUUID() + "-" + safeName);
            Path logFile = temporaryDirectory.resolve("conversion.log");

            Files.copy(
                    new java.io.ByteArrayInputStream(sourceBytes),
                    inputFile,
                    StandardCopyOption.REPLACE_EXISTING
            );

            ProcessBuilder processBuilder = new ProcessBuilder(
                    sofficePath,
                    "--headless",
                    "--nologo",
                    "--nodefault",
                    "--nofirststartwizard",
                    "--nolockcheck",
                    "--convert-to",
                    "pdf",
                    "--outdir",
                    temporaryDirectory.toString(),
                    inputFile.toString()
            );
            processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput(logFile.toFile());

            Process process = processBuilder.start();
            boolean completed = process.waitFor(timeout.toSeconds(), TimeUnit.SECONDS);

            if (!completed) {
                process.destroyForcibly();
                throw new DocumentConversionException(
                        "Document conversion timed out for " + originalFileName
                );
            }

            String processOutput = Files.exists(logFile)
                    ? Files.readString(logFile, StandardCharsets.UTF_8)
                    : "";

            if (process.exitValue() != 0) {
                throw new DocumentConversionException(
                        "LibreOffice could not convert " + originalFileName
                                + conversionDetails(processOutput)
                );
            }

            Path outputPdf;
            try (Stream<Path> stream = Files.list(temporaryDirectory)) {
                outputPdf = stream
                        .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".pdf"))
                        .findFirst()
                        .orElseThrow(() -> new DocumentConversionException(
                                "LibreOffice did not produce a PDF for " + originalFileName
                                        + conversionDetails(processOutput)
                        ));
            }

            byte[] convertedBytes = Files.readAllBytes(outputPdf);
            if (convertedBytes.length == 0) {
                throw new DocumentConversionException(
                        "LibreOffice produced an empty PDF for " + originalFileName
                );
            }

            return convertedBytes;
        } catch (DocumentConversionException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new DocumentConversionException(
                    "Document conversion failed for " + originalFileName
                            + ". Confirm that LibreOffice is installed and SOFFICE_PATH is correct.",
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new DocumentConversionException(
                    "Document conversion was interrupted for " + originalFileName,
                    exception
            );
        } finally {
            deleteRecursively(temporaryDirectory);
        }
    }

    private String safeFileName(String originalFileName) {
        String name = originalFileName == null ? "document.docx" : originalFileName;
        name = name.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1);
        name = name.replaceAll("[\\p{Cntrl}]", "").trim();
        return name.isBlank() ? "document.docx" : name;
    }

    private String conversionDetails(String output) {
        if (output == null || output.isBlank()) {
            return "";
        }
        String compact = output.replaceAll("\\s+", " ").trim();
        if (compact.length() > 300) {
            compact = compact.substring(0, 300);
        }
        return ". LibreOffice output: " + compact;
    }

    private void deleteRecursively(Path directory) {
        if (directory == null || !Files.exists(directory)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // Temporary-file cleanup failure must not hide the real conversion result.
                }
            });
        } catch (IOException ignored) {
            // Temporary-file cleanup failure must not hide the real conversion result.
        }
    }
}