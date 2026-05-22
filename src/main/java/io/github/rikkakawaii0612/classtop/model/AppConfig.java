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
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("Config");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static final String CONFIG_PATH = "config.json";
    public String logPath = "teacher_logs";
    public WeeklySchedule schedule = new WeeklySchedule();
    public boolean autoStart = true;

    // TODO: test
    public String actualPath = "none";

    @JsonIgnore
    private final List<Consumer<AppConfig>> onSavedListeners = new ArrayList<>();

    public void save() {
        this.onSavedListeners.forEach(consumer -> consumer.accept(this));
        File file = getAppDirectory().resolve(CONFIG_PATH).toFile();
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
        File file = getAppDirectory().resolve(CONFIG_PATH).toFile();
        try {
            if (file.exists()) {
                AppConfig appConfig = OBJECT_MAPPER.readValue(file, AppConfig.class);
                appConfig.actualPath = file.getCanonicalPath();
                return appConfig;
            }
        } catch (IOException e) {
            LOGGER.warn("Cannot read config from file '{}': ", file.getPath(), e);
        }
        try {
            AppConfig defaultConfig = new AppConfig();
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(file, defaultConfig);
            defaultConfig.actualPath = "default";
            return defaultConfig;
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve default config: ", e);
        }
    }
    // 获取配置文件应该存放的目录（程序所在目录）
    public static Path getAppDirectory() {
        // 1. 如果是由 jpackage 生成的 EXE 启动，优先使用该属性
        String appPath = System.getProperty("jpackage.app-path");
        if (appPath != null && !appPath.isEmpty()) {
            return Paths.get(appPath).getParent(); // 返回 EXE 所在文件夹
        }

        // 2. 回退方案：开发环境中通过 jar 路径获取
        try {
            String jarPath = AppConfig.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();
            if (jarPath != null && jarPath.endsWith(".jar")) {
                return Paths.get(jarPath).getParent();
            }
        } catch (URISyntaxException _) {}

        // 3. 最终回退：工作目录（在开发 IDE 中运行时通常就是项目根目录）
        return Paths.get(System.getProperty("user.dir"));
    }
}