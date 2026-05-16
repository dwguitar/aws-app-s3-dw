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

    public static String resolveListingPrefix(String requestedPrefix, String defaultPrefix) {
        if (requestedPrefix != null && !requestedPrefix.isBlank()) {
            return requestedPrefix;
        }
        if (defaultPrefix != null && !defaultPrefix.isBlank()) {
            return defaultPrefix;
        }
        return "";
    }

    public static String resolveUploadFolder(String requestedFolder, String defaultPrefix) {
        if (requestedFolder != null && !requestedFolder.isBlank()) {
            return requestedFolder;
        }
        if (defaultPrefix == null || defaultPrefix.isBlank()) {
            return "";
        }
        String folder = defaultPrefix.trim().replace('\\', '/');
        while (folder.endsWith("/")) {
            folder = folder.substring(0, folder.length() - 1);
        }
        return folder;
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

    public static String relativeName(String key, String normalizedPrefix) {
        if (normalizedPrefix.isEmpty()) {
            return key;
        }
        return key.substring(normalizedPrefix.length());
    }
}
