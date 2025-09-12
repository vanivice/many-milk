package com.application.manymilk.service;

import com.application.manymilk.model.db.entity.Client;
import com.application.manymilk.model.db.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class NotificationService {

    private final ClientRepository clientRepository;

    public NotificationService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<Client> getInactiveClients(int days) {
        LocalDate threshold = LocalDate.now().minusDays(days);
        return clientRepository.findByLastOrderDateBefore(threshold);
    }
}