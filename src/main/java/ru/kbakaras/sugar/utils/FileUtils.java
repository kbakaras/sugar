package ru.kbakaras.sugar.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {

    public static byte[] toByteArray(File file) {
        byte[] data = null;

        try (FileInputStream in = new FileInputStream(file)) {
            data = new byte[in.available()];
            for (int read = 0; read < data.length;) {
                read += in.read(data, read, data.length - read);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    public static File writeFile(byte[] data, File file) {
        return writeFile(data, file.getParent(), file.getName());
    }

    public static File writeFile(byte[] data, String dir, String fileName) {
        File file = new File(dir, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
            fos.close();
            return file;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}