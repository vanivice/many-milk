package com.application.manymilk.service;

import com.application.manymilk.model.db.entity.Client;
import com.application.manymilk.model.db.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public List<Client> searchClientsByLast4Digits(String last4) {
        if (last4 == null || last4.length() != 4) {
            return getAllClients();
        }

        return clientRepository.findAll().stream()
                .filter(c -> {
                    String digits = c.getPhoneNumber().replaceAll("\\D", "");
                    return digits.endsWith(last4);
                })
                .toList();
    }

    public Client saveClient(Client client) {
        // Автоформат номера
        String digits = client.getPhoneNumber().replaceAll("\\D", "");
        if (digits.length() == 11 && digits.startsWith("8")) {
            client.setPhoneNumber(String.format("+7 (%s) %s-%s-%s",
                    digits.substring(1,4), digits.substring(4,7),
                    digits.substring(7,9), digits.substring(9,11)));
        }
        return clientRepository.save(client);
    }

    public List<Client> getInactiveClients(int days) {
        LocalDate threshold = LocalDate.now().minusDays(days);
        return clientRepository.findByLastOrderDateBefore(threshold);
    }

    // Получить клиента по id
    public Client getClientById(Long id) {
        return clientRepository.findById(id).orElse(null);
    }

    // Обновление клиента
    public Client updateClient(Long id, Client updatedClient) {
        Client existing = getClientById(id);
        if (existing != null) {
            existing.setName(updatedClient.getName());
            existing.setPhoneNumber(updatedClient.getPhoneNumber());
            existing.setLastOrderDate(updatedClient.getLastOrderDate());
            return saveClient(existing); // автоформат номера
        }
        return null;
    }

    // Удаление клиента
    public void deleteClient(Long id) {
        clientRepository.deleteById(id);
    }
}
