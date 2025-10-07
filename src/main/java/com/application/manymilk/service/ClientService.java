package com.application.manymilk.service;

import com.application.manymilk.model.db.entity.Client;
import com.application.manymilk.model.db.repository.ClientRepository;
import com.application.manymilk.model.dto.request.ClientRequest;
import com.application.manymilk.model.dto.response.ClientResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    // Получить всех клиентов
    public Page<ClientResponse> getAllClients(Pageable pageable) {
        return clientRepository.findAllByOrderByIdAsc(pageable)
                .map(ClientResponse::new);
    }

    // Получить клиента по id
    public ClientResponse getClientById(Long id) {
        return clientRepository.findById(id)
                .map(ClientResponse::new)
                .orElse(null);
    }

    // Поиск клиентов по телефону (вхождение)
    public List<ClientResponse> searchClientsByPhone(String phone) {

        if (phone == null || phone.trim().isEmpty()) {
            return List.of();
        }

        String digits = phone.replaceAll("\\D", ""); // оставляем только цифры

        List<ClientResponse> exactMatches = clientRepository.findByPhoneNumber(phone).stream()
                .map(ClientResponse::new)
                .toList();

        if (!exactMatches.isEmpty()) return exactMatches;

        return clientRepository.findByPhoneContaining(digits).stream()
                .map(ClientResponse::new)
                .toList();
    }

    // Поиск клиентов по нику Telegram/WhatsApp
    public List<ClientResponse> searchClientsByNick(String nick) {
        return clientRepository.findByNickContaining(nick)
                .stream()
                .map(ClientResponse::new)
                .toList();
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
                .toList();
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
    public void updateAdditionally(Long id, String additionally) {
        clientRepository.findById(id).ifPresent(client -> {
            client.setAdditionally(
                    additionally != null && !additionally.trim().isEmpty()
                            ? additionally.trim()
                            : null
            );
            clientRepository.save(client);
        });
    }

    // Удаление дополнительного номера
    public void deleteAdditionally(Long id) {
        clientRepository.findById(id).ifPresent(client -> {
            client.setAdditionally(null);
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
        client.setAdditionally(
                request.getAdditionally() != null && !request.getAdditionally().trim().isEmpty()
                        ? request.getAdditionally().trim()
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

    // подсчет клиентов
    public long countAllClients() {
        return clientRepository.count();
    }

    public long countInactiveClients(int days) {
        LocalDate cutoffDate = LocalDate.now().minusDays(days);
        return clientRepository.countInactiveClientsSince(cutoffDate);
    }
}