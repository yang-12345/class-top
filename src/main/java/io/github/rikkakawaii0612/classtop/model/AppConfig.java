package io.github.rikkakawaii0612.classtop.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rikkakawaii0612.classtop.course.WeeklySchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class AppConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("Config");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static final String CONFIG_PATH = "./config.json";
    public String classStartTime = "08:00";  // 上课时间（弹窗提示）
    public String classEndTime = "08:45";    // 下课时间（自动关闭）
    public String logPath = "./teacher_logs";
    public WeeklySchedule schedule = new WeeklySchedule();
    public boolean autoStart = true;

    public void save() {
        File file = new File(CONFIG_PATH);
        try {
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(file, this);
        } catch (IOException e) {
            LOGGER.warn("Cannot save config to file '{}': ", file.getPath(), e);
        }
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