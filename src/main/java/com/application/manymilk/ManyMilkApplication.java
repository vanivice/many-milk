package com.application.manymilk;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@SpringBootApplication
@Slf4j
public class ManyMilkApplication {
	private static final String DB_FOLDER = "./data";
	private static final String DB_FILE = "manymilk.mv.db";

	public static void main(String[] args) {

		// копируем бд при запуске
		copyDatabaseIfNotExists();

		SpringApplication.run(ManyMilkApplication.class, args);

		// Кастомные логи
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("===================== WELCOME TO MANY-MILK! =====================");
		log.info("============ PLEASE COPY AND PASTE: LOCALHOST:8080 ==============");
		log.info("=================================================================");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");

		// открываем браузер
		openBrowser("http://localhost:8080");
	}

	// метод копировния бд
	private static void copyDatabaseIfNotExists() {
		try {
			File folder = new File(DB_FOLDER);
			if (!folder.exists()) folder.mkdirs();

			File dbFile = new File(DB_FOLDER, DB_FILE);
			if (!dbFile.exists()) {
				try (InputStream in = ManyMilkApplication.class.getResourceAsStream("/db/" + DB_FILE);
					 FileOutputStream out = new FileOutputStream(dbFile)) {
					byte[] buffer = new byte[8192];
					int bytesRead;
					while ((bytesRead = in.read(buffer)) != -1) {
						out.write(buffer, 0, bytesRead);
					}
					log.info("THE DATABASE HAS BEEN SUCCESSFULLY COPIED: " + dbFile.getAbsolutePath());
				}
			}
		} catch (Exception e) {
			log.error("COULDN'T COPY THE DATABASE", e);
		}
	}

	// метод открытия браузера на localhost:8080
	private static void openBrowser(String url) {

		try {
			// Небольшая задержка, чтобы сервер поднялся
			Thread.sleep(2000);

			// cmd
			String cmd = String.format("rundll32 url.dll,FileProtocolHandler %s", url);
			Runtime.getRuntime().exec(cmd);

			log.info("=================================================================");
			log.info("THE BROWSER IS OPEN AT " + url);
			log.info("=================================================================");

		} catch (InterruptedException e) {
			log.error("DELAY ERROR BEFORE OPENING THE BROWSER", e);
			Thread.currentThread().interrupt();
		} catch (IOException e) {
			log.error("COULDN'T OPEN THE BROWSER AUTOMATICALLY", e);
		}
	}
}
