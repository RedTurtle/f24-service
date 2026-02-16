package org.f24service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.f24service.service.F24PdfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class F24ServiceApplication {
    private static final Logger logger = LoggerFactory.getLogger(F24ServiceApplication.class);

    public static void main(String[] args) {
        if (Arrays.asList(args).contains("--cli")) {
            int exitCode = 0;
            try {
                logger.info("Running in CLI mode.");
                String jsonBody = new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
                byte[] pdfBytes = new F24PdfService().generatePdf(jsonBody);
                System.out.write(pdfBytes);
                System.out.flush();
            } catch (Exception e) {
                logger.error("Error during CLI PDF generation", e);
                System.out.println(e.getMessage());
                exitCode = 1;
            }
            System.exit(exitCode);
        } else {
            SpringApplication.run(F24ServiceApplication.class, args);
        }
    }
}
