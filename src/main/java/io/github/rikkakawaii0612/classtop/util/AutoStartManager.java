package io.github.rikkakawaii0612.classtop.util;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

public class AutoStartManager {
    private static final String RUN_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\Run";
    private static final String APP_NAME = "ClassTop";

    // 检查是否已启用开机自启
    public static boolean isAutoStartEnabled() {
        return Advapi32Util.registryValueExists(
                WinReg.HKEY_CURRENT_USER, RUN_KEY, APP_NAME);
    }

    // 启用开机自启
    public static void enableAutoStart() {
        String exePath = getExePath();
        if (exePath == null || exePath.isEmpty()) {
            System.err.println("无法获取程序路径，开机自启设置失败");
            return;
        }
        // 写入注册表，值带引号以处理路径中可能的空格
        Advapi32Util.registrySetStringValue(
                WinReg.HKEY_CURRENT_USER, RUN_KEY, APP_NAME, "\"" + exePath + "\"");
    }

    // 禁用开机自启
    public static void disableAutoStart() {
        if (isAutoStartEnabled()) {
            Advapi32Util.registryDeleteValue(
                    WinReg.HKEY_CURRENT_USER, RUN_KEY, APP_NAME);
        }
    }

    // 获取当前 EXE 或 JAR 的完整路径
    private static String getExePath() {
        // 1. 如果使用 jpackage 打包成 EXE，会设置此属性
        String appPath = System.getProperty("jpackage.app-path");
        if (appPath != null && !appPath.isEmpty()) {
            return appPath;
        }

        // 2. 开发环境下通过 jar 路径推测（可选，仅供调试）
        try {
            String jarPath = AutoStartManager.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();
            if (jarPath != null && jarPath.endsWith(".jar")) {
                // 这种情况下需要 javaw -jar 来启动，不适合直接写进 Run
                return null;   // 建议返回 null，让开发阶段不真正启用自启
            }
        } catch (Exception ignored) {}

        return null;
    }
}