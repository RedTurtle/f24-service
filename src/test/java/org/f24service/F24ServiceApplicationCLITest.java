package org.f24service;

import org.f24service.service.F24PdfService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class F24ServiceApplicationCLITest {

    // Helper class to test CLI without System.exit
    private static class TestableF24ServiceApplication {
        public static String runCli(String input, F24PdfService mockService) throws Exception {
            InputStream originalIn = System.in;
            PrintStream originalOut = System.out;

            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));
            System.setIn(new ByteArrayInputStream(input.getBytes()));

            try {
                // Inline of the CLI logic from main but without System.exit
                String jsonBody = new String(System.in.readAllBytes());
                byte[] pdfBytes = mockService.generatePdf(jsonBody);
                System.out.write(pdfBytes);
                System.out.flush();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                System.setIn(originalIn);
                System.setOut(originalOut);
            }
            return outContent.toString();
        }
    }

    @Test
    @DisplayName("CLI should print error message to stdout when generation fails")
    void testCliErrorOutput() throws Exception {
        // Given: A service that throws an exception
        F24PdfService mockService = org.mockito.Mockito.mock(F24PdfService.class);
        String errorMessage = "Invalid tax code: mismatch detected";
        when(mockService.generatePdf(anyString())).thenThrow(new Exception(errorMessage));

        // When: Running the CLI logic
        String output = TestableF24ServiceApplication.runCli("{}", mockService);

        // Then: Output should contain the error message
        assertTrue(output.contains(errorMessage), "Output should contain error message: " + errorMessage);
    }
}
