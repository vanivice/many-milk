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
			// Папка data рядом с exe
			File folder = new File(System.getProperty("user.dir"), "data");
			if (!folder.exists()) folder.mkdirs();

			// Файл базы в папке data
			File dbFile = new File(folder, "manymilk.mv.db");

			if (!dbFile.exists()) {
				// Берем файл из ресурсов jar
				try (InputStream in = ManyMilkApplication.class.getResourceAsStream("/db/manymilk.mv.db");
					 FileOutputStream out = new FileOutputStream(dbFile)) {

					if (in == null) {
						log.info("=================================================================");
						log.error("THE DATABASE FILE WAS NOT FOUND IN THE RESOURCES!");
						log.info("=================================================================");
						return;
					}

					byte[] buffer = new byte[8192];
					int bytesRead;
					while ((bytesRead = in.read(buffer)) != -1) {
						out.write(buffer, 0, bytesRead);
					}
					log.info("=================================================================");
					log.info("THE DATABASE HAS BEEN SUCCESSFULLY COPIED TO: " + dbFile.getAbsolutePath());
					log.info("=================================================================");
				}
			} else {
				log.info("=================================================================");
				log.info("THE DATABASE ALREADY EXISTS: " + dbFile.getAbsolutePath());
				log.info("=================================================================");
			}
		} catch (Exception e) {
			log.info("=================================================================");
			log.error("COULDN'T COPY THE DATABASE", e);
			log.info("=================================================================");
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
			log.info("=================================================================");
			log.error("DELAY ERROR BEFORE OPENING THE BROWSER", e);
			log.info("=================================================================");
			Thread.currentThread().interrupt();
		} catch (IOException e) {
			log.info("=================================================================");
			log.error("COULDN'T OPEN THE BROWSER AUTOMATICALLY", e);
			log.info("=================================================================");
		}
	}
}
