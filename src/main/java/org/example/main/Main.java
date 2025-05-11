package org.example.main;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.logic.PaymentOptimizer;
import org.example.model.Order;
import org.example.model.PaymentMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String OUTPUT_FILENAME = "result.txt";

    public static void main(String[] args) {
        if (args.length != 2) {
            logger.error("Usage: java -jar app.jar <orders.json> <paymentmethods.json>");
            System.exit(1);
        }

        try {
            List<Order> orders = loadJsonList(args[0], new TypeReference<>() {});
            List<PaymentMethod> methods = loadJsonList(args[1], new TypeReference<>() {});

            PaymentOptimizer optimizer = new PaymentOptimizer(orders, methods);
            Map<String, BigDecimal> usage = optimizer.optimizePayments();

            printResults(usage);
            writeResultsToFile(usage);

        } catch (Exception e) {
            logger.error("Fatal error occurred", e);
            System.exit(2);
        }
    }

    private static <T> List<T> loadJsonList(String path, TypeReference<List<T>> typeRef) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(path), typeRef);
    }

    private static void printResults(Map<String, BigDecimal> usage) {
        usage.forEach((methodId, amount) ->
                System.out.println(methodId + " " + amount.setScale(2, RoundingMode.HALF_UP)));
    }

    private static void writeResultsToFile(Map<String, BigDecimal> usage) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(OUTPUT_FILENAME))) {
            for (Map.Entry<String, BigDecimal> entry : usage.entrySet()) {
                String line = entry.getKey() + " " + entry.getValue().setScale(2, RoundingMode.HALF_UP);
                writer.write(line);
                writer.newLine();
            }
            logger.info("Results also saved to file: {}", OUTPUT_FILENAME);
        } catch (Exception e) {
            logger.error("Could not save to file '{}': {}", OUTPUT_FILENAME, e.getMessage());
        }
    }
}