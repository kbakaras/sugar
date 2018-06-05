package org.butu.sugar.utils;

import org.butu.sugar.lazy.Lazy;

public class OSDetector {
    private static Lazy<Boolean> isWinOS = Lazy.of(
            () -> System.getProperty("os.name").toLowerCase().contains("win"));
    private static Lazy<Boolean> isMacOS = Lazy.of(
            () -> System.getProperty("os.name").toLowerCase().contains("mac"));
    private static Lazy<Boolean> isLinux = Lazy.of(
            () -> {
                String osName = System.getProperty("os.name").toLowerCase();
                return osName.contains("nix") || osName.contains("linux");
            });

    public static boolean isWinOS() {
        return isWinOS.get();
    }
    
    public static boolean isMacOS() {
        return isMacOS.get();
    }
    
    public static boolean isLinux() {
        return isLinux.get();
    }
}