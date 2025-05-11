package org.example.main;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MainIntegrationTest {

    private static final Path OUTPUT = Path.of("result.txt");

    @AfterEach
    void cleanup() throws Exception {
        Files.deleteIfExists(OUTPUT);
    }

    @Test
    void testMainApplication() throws Exception {
        String ordersPath = getResourcePath("orders.json");
        String methodsPath = getResourcePath("paymentmethods.json");

        Main.main(new String[]{ordersPath, methodsPath});

        assertTrue(Files.exists(OUTPUT), "Output file was not created.");

        Map<String, BigDecimal> result = readResultFile(OUTPUT.toFile());

        assertEquals(new BigDecimal("165.00"), result.get("mZysk"));
        assertEquals(new BigDecimal("190.00"), result.get("BosBankrut"));
        assertEquals(new BigDecimal("100.00"), result.get("PUNKTY"));
    }

    private String getResourcePath(String fileName) {
        return getClass().getClassLoader().getResource(fileName).getFile();
    }

    private Map<String, BigDecimal> readResultFile(File file) throws Exception {
        Map<String, BigDecimal> result = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.strip().split(" ");
                result.put(parts[0], new BigDecimal(parts[1]));
            }
        }
        return result;
    }
}