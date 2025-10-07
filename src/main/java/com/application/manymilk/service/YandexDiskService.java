package com.application.manymilk.service;

import com.application.manymilk.config.YandexDiskConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.Statement;

@Service
@Slf4j
public class YandexDiskService {

    private final YandexDiskConfig config;
    private final DataSource dataSource;

    public YandexDiskService(YandexDiskConfig config, DataSource dataSource) {
        this.config = config;
        this.dataSource = dataSource;
    }

    public void uploadDatabase() throws Exception {
        // Временный файл резервной копии
        File backupFile = new File(System.getProperty("java.io.tmpdir"), "manymilk_backup.zip");

        // Горячее резервное копирование H2
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("BACKUP TO '" + backupFile.getAbsolutePath().replace("\\", "/") + "'");
        }

        // Получаем ссылку для загрузки на Яндекс.Диск
        String uploadUrl = "https://cloud-api.yandex.net/v1/disk/resources/upload?path=backup/manymilk_backup.zip&overwrite=true";
        HttpURLConnection conn = (HttpURLConnection) new URL(uploadUrl).openConnection();
        conn.setRequestProperty("Authorization", "OAuth " + config.getToken());
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Ошибка получения ссылки для загрузки: " + conn.getResponseMessage());
        }

        String json = new String(conn.getInputStream().readAllBytes());
        String href = json.split("\"href\":\"")[1].split("\"")[0].replace("\\u0026", "&");

        // Загружаем файл на полученный href
        HttpURLConnection uploadConn = (HttpURLConnection) new URL(href).openConnection();
        uploadConn.setDoOutput(true);
        uploadConn.setRequestMethod("PUT");

        try (FileInputStream fis = new FileInputStream(backupFile);
             OutputStream os = uploadConn.getOutputStream()) {

            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
        }

        if (uploadConn.getResponseCode() != 201) {
            throw new RuntimeException("Ошибка при загрузке файла: " + uploadConn.getResponseMessage());
        }

        log.info("База успешно загружена на Яндекс.Диск");

        // Удаляем временный файл после успешной загрузки
        backupFile.delete();
    }
}
