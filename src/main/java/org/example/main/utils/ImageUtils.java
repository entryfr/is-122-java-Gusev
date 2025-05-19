package org.example.main.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

public class ImageUtils {

    /**
     * Загружает изображение из файла и возвращает его как массив байтов.
     *
     * @param path путь к файлу изображения
     * @return массив байтов, представляющий изображение
     * @throws IOException если произошла ошибка при чтении файла
     */
    public static byte[] loadImage(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            throw new IOException("Файл не найден: " + path);
        }
        return Files.readAllBytes(file.toPath());
    }

    /**
     * Получает расширение файла из его имени.
     *
     * @param fileName имя файла
     * @return расширение файла
     */
    private static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "png";
        }
        int lastIndex = fileName.lastIndexOf('.');
        if (lastIndex == -1) {
            return "png";
        }
        return fileName.substring(lastIndex + 1).toLowerCase();
    }
}