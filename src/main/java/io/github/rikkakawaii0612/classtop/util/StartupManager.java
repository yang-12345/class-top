package io.github.rikkakawaii0612.classtop.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class StartupManager {
    private static final String REG_PATH = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";
    private final String entryName;
    private final String command;   // 最终要写入注册表的完整命令

    /**
     * 工厂方法：自动检测当前运行环境，生成最合适的启动命令。
     * @param entryName 注册表中的键名，例如 "MyJavaFXApp"
     * @return StartupManager 实例
     * @throws IllegalStateException 如果无法确定启动命令（如 IDE 直接运行类文件）
     */
    public static StartupManager createAutoDetect(String entryName) {
        String command = detectStartupCommand();
        return new StartupManager(entryName, command);
    }

    /**
     * 手动指定启动命令（适用于你明确知道应该用什么命令启动）
     */
    public StartupManager(String entryName, String command) {
        this.entryName = entryName;
        this.command = command;
    }

    // ----------------- 自启状态管理 -----------------
    public boolean isEnabled() {
        try {
            Process p = Runtime.getRuntime().exec(
                    new String[]{"reg", "query", REG_PATH, "/v", entryName});
            return p.waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    public void enable() {
        try {
            Process p = Runtime.getRuntime().exec(
                    new String[]{"reg", "add", REG_PATH, "/v", entryName,
                            "/t", "REG_SZ", "/d", command, "/f"});
            p.waitFor();
        } catch (IOException | InterruptedException _) {
        }
    }

    public void disable() {
        try {
            Process p = Runtime.getRuntime().exec(
                    new String[]{"reg", "delete", REG_PATH, "/v", entryName, "/f"});
            int code = p.waitFor();
        } catch (IOException | InterruptedException _) {
        }
    }

    // ----------------- 启动命令自动检测逻辑 -----------------
    private static String detectStartupCommand() {
        // 1. 获取启动当前 JVM 的可执行文件路径
        Optional<String> procCommand = ProcessHandle.current()
                .info()
                .command();

        if (procCommand.isEmpty()) {
            throw new IllegalStateException("无法获取当前进程的命令行，请手动指定启动命令。");
        }

        Path executablePath = Paths.get(procCommand.get());
        String executableName = executablePath.getFileName().toString().toLowerCase();

        // 2. 判断是否为 jpackage 生成的原生启动器
        if (!executableName.equals("java.exe") && !executableName.equals("javaw.exe")) {
            // 直接使用该 exe 的完整路径，路径中空格已由 Path 自动处理
            return "\"" + executablePath + "\"";
        }

        // 3. 如果是 java/javaw，尝试定位当前 JAR 包路径
        Optional<Path> jarPath = getCurrentJarPath();
        if (jarPath.isPresent()) {
            // 使用 javaw.exe 以无控制台窗口方式运行
            Path javaHome = Paths.get(System.getProperty("java.home"));
            Path javaw = javaHome.resolve("bin").resolve("javaw.exe");
            return "\"" + javaw + "\" -jar \"" + jarPath.get() + "\"";
        }

        // 4. 无法找到 JAR（比如 IDE 直接运行 .class 文件）
        throw new IllegalStateException(
                "当前运行环境不是 JAR 包，无法自动生成开机自启命令。请先使用 jpackage 打包为 exe，"
                        + "或手动调用 new StartupManager(entryName, yourCommand) 指定命令。");
    }

    /**
     * 获取当前代码所在的 JAR 文件路径（如果从 JAR 运行）
     */
    private static Optional<Path> getCurrentJarPath() {
        try {
            Path path = Paths.get(StartupManager.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
            if (path.toString().toLowerCase().endsWith(".jar")) {
                return Optional.of(path);
            }
        } catch (URISyntaxException | SecurityException ignored) {
        }
        return Optional.empty();
    }
}