package com.application.manymilk.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

@Controller
@RequiredArgsConstructor
@Slf4j // CHANGE: добавлено для логирования
public class LoginController {

    private static final int QR_SIZE = 300; // CHANGE: вынесён размер QR в константу

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping(value = "/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public void generateQr(HttpServletResponse response) {
        try {
            // Получаем локальный IPv4
            String ip = getLocalIPv4();
            String url = "http://" + ip + ":8080/login";

            // Генерация QR-кода
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);

            response.setContentType(MediaType.IMAGE_PNG_VALUE);
            response.setHeader("Cache-Control", "no-cache");

            // CHANGE: безопасное закрытие потока через try-with-resources
            try (OutputStream os = response.getOutputStream()) {
                MatrixToImageWriter.writeToStream(bitMatrix, "PNG", os);
                os.flush();
            }

        } catch (Exception e) {
            log.error("Ошибка генерации QR-кода", e); // CHANGE: логирование вместо printStackTrace
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка генерации QR-кода");
            } catch (Exception ex) {
                log.error("Ошибка отправки ответа с ошибкой QR-кода", ex);
            }
        }
    }

    // Метод для получения локального IPv4
    private String getLocalIPv4() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!ni.isUp() || ni.isLoopback()) continue;

                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Ошибка определения локального IP", e); // CHANGE: логирование
        }
        return "127.0.0.1"; // fallback
    }
}
