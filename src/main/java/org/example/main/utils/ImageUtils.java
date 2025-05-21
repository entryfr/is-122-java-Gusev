package org.example.main.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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

}