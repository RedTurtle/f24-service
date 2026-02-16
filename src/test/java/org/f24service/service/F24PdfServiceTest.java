package org.f24service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for F24PdfService with focus on tax code validation.
 *
 * Italian tax code (Codice Fiscale) structure:
 * - 3 letters: Surname consonants
 * - 3 letters: Name consonants
 * - 2 digits: Birth year (last 2 digits)
 * - 1 letter: Birth month and sex
 * - 2 digits: Birth day
 * - 4 chars: Municipality code
 * - 1 letter: Check digit
 */
class F24PdfServiceTest {

    private final F24PdfService pdfService = new F24PdfService();

    @Test
    @DisplayName("Valid tax code should generate PDF successfully")
    void testValidTaxCode() throws Exception {
        // Given: A JSON with a valid tax code
        String jsonContent = loadTestResource("valid-tax-code.json");

        // When: Generate PDF
        byte[] pdfBytes = pdfService.generatePdf(jsonContent);

        // Then: PDF should be generated successfully
        assertNotNull(pdfBytes, "PDF bytes should not be null");
        assertTrue(pdfBytes.length > 0, "PDF should have content");

        // PDF files start with '%PDF-'
        String pdfHeader = new String(pdfBytes, 0, Math.min(5, pdfBytes.length));
        assertEquals("%PDF-", pdfHeader, "Generated file should be a valid PDF");
    }

    @Test
    @DisplayName("Valid tax code with wrong birthlocation should generate PDF successfully")
    void testValidTaxCodeWithWrongBirthlocation() throws Exception {
        // Given: A JSON with a valid tax code but wrong birthlocation
        // --> but birthlocation is not verified so it's ok
        String jsonContent = loadTestResource("valid-tax-code-with-wrong-birthlocation.json");

        // When: Generate PDF
        byte[] pdfBytes = pdfService.generatePdf(jsonContent);

        // Then: PDF should be generated successfully
        assertNotNull(pdfBytes, "PDF bytes should not be null");
        assertTrue(pdfBytes.length > 0, "PDF should have content");

        // PDF files start with '%PDF-'
        String pdfHeader = new String(pdfBytes, 0, Math.min(5, pdfBytes.length));
        assertEquals("%PDF-", pdfHeader, "Generated file should be a valid PDF");
    }

    @Test
    @DisplayName("Invalid tax code - swapped name and surname should fail")
    void testInvalidTaxCodeSwappedNameSurname() throws Exception {
        // Given: Tax code MRARSS80A01C704B with name MARIO and surname ROSSI
        // Problem: MRA (from MARIO) is in surname position, RSS (from ROSSI) is in name
        // position
        // Correct code should be: RSSMRA80A01C704B
        String jsonContent = loadTestResource("invalid-swapped-names.json");

        // When & Then: Should throw exception about invalid tax code
        Exception exception = assertThrows(Exception.class, () -> {
            pdfService.generatePdf(jsonContent);
        });

        // Verify the error message contains information about tax code mismatch
        String message = exception.getMessage();
        assertTrue(
                message.contains("Invalid tax code") || message.contains("not corresponds"),
                "Exception should mention invalid tax code. Got: " + message);
    }

    @Test
    @DisplayName("Invalid tax code - wrong birth year should fail")
    void testInvalidTaxCodeWrongBirthYear() throws Exception {
        // Given: Tax code with birth year 85 but personal data says 1980
        String jsonContent = loadTestResource("invalid-wrong-birthdate.json");

        // When & Then: Should throw exception about invalid tax code
        Exception exception = assertThrows(Exception.class, () -> {
            pdfService.generatePdf(jsonContent);
        });

        // Verify the error message
        String message = exception.getMessage();
        assertTrue(
                message.contains("Invalid tax code") || message.contains("not corresponds"),
                "Exception should mention invalid tax code. Got: " + message);
    }

    @Test
    @DisplayName("Valid F24 simplified should generate complete PDF")
    void testValidF24SimplifiedGeneration() throws Exception {
        // Given: Valid F24 simplified data from examples
        String jsonContent = Files.readString(
                java.nio.file.Path.of("examples/f24simplified.json"));

        // When: Generate PDF
        byte[] pdfBytes = pdfService.generatePdf(jsonContent);

        // Then: PDF should be generated and have reasonable size
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 100, "PDF should have substantial content");

        // Verify it's a valid PDF
        String pdfHeader = new String(pdfBytes, 0, 5);
        assertEquals("%PDF-", pdfHeader);
    }

    @Test
    @DisplayName("Empty JSON should fail gracefully")
    void testEmptyJson() {
        // When & Then: Should throw exception
        assertThrows(Exception.class, () -> {
            pdfService.generatePdf("");
        });
    }

    @Test
    @DisplayName("Malformed JSON should fail gracefully")
    void testMalformedJson() {
        // When & Then: Should throw exception
        assertThrows(Exception.class, () -> {
            pdfService.generatePdf("{invalid json}");
        });
    }

    /**
     * Helper method to load test resources
     */
    private String loadTestResource(String resourcePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        return Files.readString(resource.getFile().toPath());
    }
}
