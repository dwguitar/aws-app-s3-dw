package com.dw.awsapps3dw.util;

public final class S3PathUtils {

    private S3PathUtils() {}

    public static String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "";
        }
        String normalized = prefix.trim().replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized.endsWith("/") ? normalized : normalized + "/";
    }

    public static String buildObjectKey(String folder, String fileName) {
        String prefix = normalizePrefix(folder);
        String safeName = fileName.trim().replace('\\', '/');
        while (safeName.startsWith("/")) {
            safeName = safeName.substring(1);
        }
        if (safeName.contains("/")) {
            throw new IllegalArgumentException("Nome do arquivo não pode conter barras.");
        }
        return prefix + safeName;
    }
}
