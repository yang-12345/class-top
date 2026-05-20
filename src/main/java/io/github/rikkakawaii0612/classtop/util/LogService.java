package io.github.rikkakawaii0612.classtop.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.time.*;

public class LogService {
    private static final Logger LOGGER = LoggerFactory.getLogger("Config");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Path logDir;

    public LogService(String logDirPath) {
        this.logDir = Paths.get(logDirPath);
        try {
            Files.createDirectories(logDir);
        } catch (Exception e) {
            LOGGER.warn("Failed to create directories for path '{}': ", logDirPath, e);
        }
    }

    // 记录点击事件
    public void logButtonClick(LocalDate date, LocalTime time, boolean isClicked) {
        try {
            ObjectNode node = OBJECT_MAPPER.createObjectNode();
            node.put("date", date.toString());
            node.put("time", time.toString());
            node.put("isClicked", isClicked);
            node.put("type", "clicked");

            String fileName = date + "_clicked.json";
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValue(logDir.resolve(fileName).toFile(), node);
            LOGGER.info("Recorded confirmation of filling classroom log");
        } catch (Exception e) {
            LOGGER.error("Caught exception while logging button click event: ", e);
        }
    }

    // 记录未点击
    public void logUnclicked(LocalDate date, LocalTime time) {
        try {
            ObjectNode node = OBJECT_MAPPER.createObjectNode();
            node.put("date", date.toString());
            node.put("closeTime", time.toString());
            node.put("isClicked", false);
            node.put("type", "unclicked");

            String fileName = date + "_unclicked.json";
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValue(logDir.resolve(fileName).toFile(), node);
            LOGGER.info("Recorded neglect of classroom log");
        } catch (Exception e) {
            LOGGER.error("Caught exception while logging button unclicked event: ", e);
        }
    }
}