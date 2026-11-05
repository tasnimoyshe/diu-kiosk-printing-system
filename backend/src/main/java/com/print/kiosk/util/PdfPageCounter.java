package com.print.kiosk.util;

import com.print.kiosk.exception.FileValidationException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PdfPageCounter {

    public int countPages(byte[] pdfBytes, String displayName) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new FileValidationException(displayName + " is empty");
        }

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            if (document.isEncrypted()) {
                throw new FileValidationException(
                        displayName + " is password-protected. Please upload an unlocked PDF."
                );
            }

            int pageCount = document.getNumberOfPages();
            if (pageCount <= 0) {
                throw new FileValidationException(displayName + " does not contain any printable pages");
            }
            return pageCount;
        } catch (InvalidPasswordException exception) {
            throw new FileValidationException(
                    displayName + " is password-protected. Please upload an unlocked PDF.",
                    exception
            );
        } catch (IOException exception) {
            throw new FileValidationException(
                    displayName + " is corrupted or is not a valid PDF",
                    exception
            );
        }
    }
}