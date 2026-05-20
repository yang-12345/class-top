package io.github.rikkakawaii0612.classtop.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("Config");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static final String CONFIG_PATH = "./config.json";
    public String logPath = "./teacher_logs";
    public WeeklySchedule schedule = new WeeklySchedule();
    public boolean autoStart = true;

    @JsonIgnore
    private final List<Consumer<AppConfig>> onSavedListeners = new ArrayList<>();

    public void save() {
        this.onSavedListeners.forEach(consumer -> consumer.accept(this));
        File file = new File(CONFIG_PATH);
        try {
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(file, this);
        } catch (IOException e) {
            LOGGER.warn("Cannot save config to file '{}': ", file.getPath(), e);
        }
    }

    public void subscribeOnSaved(Consumer<AppConfig> action) {
        this.onSavedListeners.add(action);
    }

    public static AppConfig loadConfig() {
        File file = new File(CONFIG_PATH);
        try {
            if (file.exists()) {
                return OBJECT_MAPPER.readValue(file, AppConfig.class);
            }
        } catch (IOException e) {
            LOGGER.warn("Cannot read config from file '{}': ", file.getPath(), e);
        }
        try {
            AppConfig defaultConfig = new AppConfig();
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(file, defaultConfig);
            return defaultConfig;
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve default config: ", e);
        }
    }
}