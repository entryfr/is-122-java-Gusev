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
     * Сохраняет изображение в указанной директории с уникальным именем файла.
     *
     * @param imageData массив байтов, представляющий изображение
     * @param uploadDir путь к директории для сохранения изображения
     * @param originalFileName оригинальное имя файла (для определения расширения)
     * @return имя сохранённого файла
     * @throws IOException если произошла ошибка при записи файла
     */
    public static String saveImage(byte[] imageData, String uploadDir, String originalFileName) throws IOException {
        // Создаём объект File для директории
        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists()) {
            if (!uploadDirectory.mkdirs()) {
                throw new IOException("Не удалось создать директорию: " + uploadDir);
            }
        }

        String extension = getFileExtension(originalFileName);
        String fileName = UUID.randomUUID().toString() + "." + extension;

        File outputFile = new File(uploadDirectory, fileName);
        Files.write(outputFile.toPath(), imageData);

        return fileName;
    }

    /**
     * Получает расширение файла из его имени.
     *
     * @param fileName имя файла
     * @return расширение файла (например, "png", "jpg")
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