package com.application.manymilk.service;

import com.application.manymilk.model.db.entity.Client;
import com.application.manymilk.model.db.repository.ClientRepository;
import com.application.manymilk.model.dto.request.ClientRequest;
import com.application.manymilk.model.dto.response.ClientResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    // Получить всех клиентов
    public List<ClientResponse> getAllClients() {
        return clientRepository.findAllByOrderByIdAsc().stream()
                .map(ClientResponse::new)
                .collect(Collectors.toList());
    }

    // Получить клиента по id
    public ClientResponse getClientById(Long id) {
        return clientRepository.findById(id)
                .map(ClientResponse::new)
                .orElse(null);
    }

    // Поиск клиентов по последним 4 цифрам телефона
    public List<ClientResponse> searchClientsByPhone(String phone) {

        if (phone == null || phone.trim().isEmpty()) {
            return getAllClients();
        }

        String input = phone.trim();

        // убираем все нецифровые символы для сравнения последних цифр
        String digitsInput = input.replaceAll("\\D", "");
        String last4 = digitsInput.length() >= 4
                ? digitsInput.substring(digitsInput.length() - 4)
                : digitsInput;

        // ищем точное совпадение по формату
        List<ClientResponse> exactMatches = clientRepository.findAll().stream()
                .filter(c -> c.getPhoneNumber() != null &&
                        c.getPhoneNumber().equals(input))
                .map(ClientResponse::new)
                .collect(Collectors.toList());

        if (!exactMatches.isEmpty()) {
            return exactMatches;
        }

        // Если точных совпадений нет — ищем по последним 4 цифрам
        return clientRepository.findAll().stream()
                .filter(c -> c.getPhoneNumber() != null &&
                        c.getPhoneNumber().replaceAll("\\D", "").endsWith(last4))
                .map(ClientResponse::new)
                .collect(Collectors.toList());
    }

    // Поиск клиентов по нику Telegram/WhatsApp
    public List<ClientResponse> searchClientsByNick(String nick) {
        if (nick == null || nick.isEmpty()) return getAllClients();

        String lowerNick = nick.toLowerCase();
        return clientRepository.findAll().stream()
                .filter(c -> (c.getTelegramNick() != null && c.getTelegramNick().toLowerCase().contains(lowerNick))
                        || (c.getWhatsAppNick() != null && c.getWhatsAppNick().toLowerCase().contains(lowerNick)))
                .map(ClientResponse::new)
                .collect(Collectors.toList());
    }

    // Сохранение нового клиента из DTO
    public ClientResponse createClient(ClientRequest request) {
        String phone = request.getPhoneNumber();

        // Проверка на null/пустой
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Номер телефона не может быть пустым");
        }

        // Проверка уникальности
        if (clientRepository.existsByPhoneNumber(phone)) {
            throw new IllegalArgumentException("Клиент с таким номером телефона уже существует");
        }

        Client client = new Client();
        fillClientFromRequest(client, request);

        return new ClientResponse(clientRepository.save(client));
    }

    // Получить неактивных клиентов
    public List<ClientResponse> getInactiveClients(int days) {
        LocalDate threshold = LocalDate.now().minusDays(days);
        return clientRepository.findByLastOrderDateBefore(threshold).stream()
                .map(ClientResponse::new)
                .collect(Collectors.toList());
    }

    // Обновление клиента по id
    public ClientResponse updateClient(Long id, ClientRequest request) {
        Client client = clientRepository.findById(id).orElse(null);
        if (client != null) {
            // Заполняем остальные поля из DTO
            fillClientFromRequest(client, request);

            // Обработка даты последнего заказа
            if (request.getLastOrderDate() != null && !request.getLastOrderDate().trim().isEmpty()) {
                client.setLastOrderDate(LocalDate.parse(request.getLastOrderDate()));
            }
            // если поле пустое — оставляем существующую дату без изменений

            return new ClientResponse(clientRepository.save(client));
        }
        return null;
    }

    // Отметка "сделал заказ сегодня"
    public void markOrderToday(Long id) {
        clientRepository.findById(id).ifPresent(client -> {
            client.setLastOrderDate(LocalDate.now());
            clientRepository.save(client);
        });
    }

    // Обновление дополнительного номера
    public void updateSecondaryPhone(Long id, String secondaryPhoneNumber) {
        clientRepository.findById(id).ifPresent(client -> {
            client.setSecondaryPhoneNumber(
                    secondaryPhoneNumber != null && !secondaryPhoneNumber.trim().isEmpty()
                            ? secondaryPhoneNumber.trim()
                            : null
            );
            clientRepository.save(client);
        });
    }

    // Удаление дополнительного номера
    public void deleteSecondaryPhone(Long id) {
        clientRepository.findById(id).ifPresent(client -> {
            client.setSecondaryPhoneNumber(null);
            clientRepository.save(client);
        });
    }

    // Удаление клиента
    public void deleteClient(Long id) {
        clientRepository.deleteById(id);
    }

    // --- Вспомогательный метод заполнения клиента из DTO ---
    private void fillClientFromRequest(Client client, ClientRequest request) {
        client.setName(request.getName());
        client.setPhoneNumber(formatPhoneNumber(request.getPhoneNumber()));
        client.setSecondaryPhoneNumber(
                request.getSecondaryPhoneNumber() != null && !request.getSecondaryPhoneNumber().trim().isEmpty()
                        ? request.getSecondaryPhoneNumber().trim()
                        : null
        );
        client.setTelegramNick(
                request.getTelegramNick() != null && !request.getTelegramNick().trim().isEmpty()
                        ? request.getTelegramNick().trim()
                        : null
        );
        client.setWhatsAppNick(
                request.getWhatsAppNick() != null && !request.getWhatsAppNick().trim().isEmpty()
                        ? request.getWhatsAppNick().trim()
                        : null
        );
        if (client.getLastOrderDate() == null) {
            client.setLastOrderDate(null);
        }
    }

    // --- Вспомогательный метод автоформата номера ---
//    private String formatPhoneNumber(String phone) {
//        if (phone == null) return null;
//        String digits = phone.replaceAll("\\D", "");
//        if (digits.length() == 11 && digits.startsWith("8")) {
//            return String.format("+7 (%s) %s-%s-%s",
//                    digits.substring(1, 4),
//                    digits.substring(4, 7),
//                    digits.substring(7, 9),
//                    digits.substring(9, 11));
//        }
//        return phone;
//    }
    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null; // или возвращай старое значение
        }
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() == 11 && (digits.startsWith("7") || digits.startsWith("8"))) {
            return String.format("+7 (%s) %s-%s-%s",
                    digits.substring(1, 4),
                    digits.substring(4, 7),
                    digits.substring(7, 9),
                    digits.substring(9, 11));
        }
        throw new IllegalArgumentException("Неверный формат номера: " + phone);
    }
}