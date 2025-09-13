package com.application.manymilk.controller;

import com.application.manymilk.model.db.entity.Client;
import com.application.manymilk.model.db.repository.ClientRepository;
import com.application.manymilk.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/clients")
public class ClientController {

    private final ClientRepository clientRepository;
    private final ClientService clientService;

    public ClientController(ClientRepository clientRepository, ClientService clientService) {
        this.clientRepository = clientRepository;
        this.clientService = clientService;
    }

//    // Список всех клиентов
//    @GetMapping
//    public String listClients(Model model) {
//        model.addAttribute("clients", clientRepository.findAll());
//        return "clients";
//    }

    //*****
    @GetMapping
    public String listClients(Model model) {
        model.addAttribute("clients", clientRepository.findAllByOrderByIdAsc());
        return "clients";
    }

    // Поиск клиентов по последним 4 цифрам или полному номеру
    @GetMapping("/search")
    public String searchClient(@RequestParam String phone, Model model) {
        String digitsInput = phone.replaceAll("\\D", "");
        String last4Digits = digitsInput.length() >= 4
                ? digitsInput.substring(digitsInput.length() - 4)
                : digitsInput;

        List<Client> clients = clientRepository.findAll().stream()
                .filter(c -> c.getPhoneNumber() != null &&
                        c.getPhoneNumber().replaceAll("\\D","")
                                .endsWith(last4Digits))
                .collect(Collectors.toList());

        if (clients.isEmpty()) {
            model.addAttribute("notFoundMessage", "Клиент с таким номером не найден");
        }

        model.addAttribute("clients", clients);
        return "clients";
    }

    // Показ формы добавления клиента
    @GetMapping("/add")
    public String showAddClientForm(Model model) {
        model.addAttribute("client", new Client());
        return "add_client";
    }

    // Добавление клиента с валидацией
    @PostMapping("/add")
    public String addClient(@Valid @ModelAttribute Client client,
                            BindingResult bindingResult,
                            Model model) {
        if (bindingResult.hasErrors()) return "add_client";

        formatPhoneNumber(client);

        // Присвоение null пустым полям
        if (client.getLastOrderDate() == null) {
            client.setLastOrderDate(LocalDate.of(2025, 01, 01));
            // поле уже null по умолчанию, можно оставить
        }

        if (client.getTelegramNick() != null && client.getTelegramNick().trim().isEmpty()) {
            client.setTelegramNick(null);
        }

        if (client.getWhatsAppNick() != null && client.getWhatsAppNick().trim().isEmpty()) {
            client.setWhatsAppNick(null);
        }

        clientRepository.save(client);
        return "redirect:/clients";
    }

    // Показ формы редактирования клиента
    @GetMapping("/edit/{id}")
    public String showEditClientForm(@PathVariable Long id, Model model) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid client Id:" + id));
        model.addAttribute("client", client);
        return "edit_client";
    }

    // Обновление клиента с валидацией
//    @PostMapping("/edit/{id}")
//    public String updateClient(@PathVariable Long id,
//                               @Valid @ModelAttribute Client client,
//                               BindingResult bindingResult) {
//        if (bindingResult.hasErrors()) return "edit_client";
//
//        // Получаем существующего клиента из базы
//        Client existingClient = clientRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid client Id:" + id));
//
//        // Обновляем поля, пришедшие с формы
//        existingClient.setName(client.getName());
//        existingClient.setPhoneNumber(client.getPhoneNumber());
//        existingClient.setLastOrderDate(client.getLastOrderDate());
//        existingClient.setTelegramNick(client.getTelegramNick() != null && !client.getTelegramNick().trim().isEmpty()
//                ? client.getTelegramNick() : null);
//        existingClient.setWhatsAppNick(client.getWhatsAppNick() != null && !client.getWhatsAppNick().trim().isEmpty()
//                ? client.getWhatsAppNick() : null);
//
//        // Не трогаем secondaryPhoneNumber, чтобы он не удалялся
//
//        formatPhoneNumber(existingClient);
//
//        clientRepository.save(existingClient);
//        return "redirect:/clients"; // порядок по id останется
//    }

    @PostMapping("/edit/{id}")
    public String updateClient(@PathVariable Long id,
                               @Valid @ModelAttribute Client client,
                               BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "edit_client";
        }

        Client existing = clientService.getClientById(id);
        if (existing != null) {
            existing.setName(client.getName());
            existing.setPhoneNumber(client.getPhoneNumber());
            existing.setTelegramNick(client.getTelegramNick());
            existing.setWhatsAppNick(client.getWhatsAppNick());
            // Сохраняем дату последнего заказа только если она не пустая
            if (client.getLastOrderDate() != null) {
                existing.setLastOrderDate(client.getLastOrderDate());
            }

            clientService.saveClient(existing); // автоформат номера
        }

        return "redirect:/clients";
    }

    // Удаление клиента
    @GetMapping("/delete/{id}")
    public String deleteClient(@PathVariable Long id) {
        clientRepository.deleteById(id);
        return "redirect:/clients";
    }

    // Сделал заказ сегодня
    @PostMapping("/order-today/{id}")
    public String markOrderToday(@PathVariable("id") Long id,
                                 @RequestHeader(value = "referer", required = false) String referer) {
        Client client = clientService.getClientById(id);
        if (client != null) {
            client.setLastOrderDate(LocalDate.now());
            clientService.saveClient(client);
        }
        if (referer != null) {
            return "redirect:" + referer;
        }
        return "redirect:/clients";
    }

    // Список неактивных клиентов
    @GetMapping("/inactive")
    public String inactiveClients(Model model) {
        List<Client> inactive = clientService.getInactiveClients(30);
        model.addAttribute("clients", inactive);
        return "inactive_clients";
    }

    //*****
    // Показ формы редактирования доп. номера (через AJAX/Modal можно реализовать)
    @PostMapping("/secondary-phone/{id}")
    public String updateSecondaryPhone(@PathVariable Long id,
                                       @RequestParam String secondaryPhoneNumber) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid client Id:" + id));
        client.setSecondaryPhoneNumber(secondaryPhoneNumber.trim().isEmpty() ? null : secondaryPhoneNumber);
        clientRepository.save(client);
        return "redirect:/clients"; // редирект на список с сортировкой по id
    }

    // Удаление доп. номера
    @GetMapping("/secondary-phone/delete/{id}")
    public String deleteSecondaryPhone(@PathVariable Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid client Id:" + id));
        client.setSecondaryPhoneNumber(null);
        clientRepository.save(client);
        return "redirect:/clients"; // редирект на список с сортировкой по id
    }
    //*****

    // Поиск по нику Telegram или WhatsApp
    @GetMapping("/searchByNick")
    public String searchByNick(@RequestParam String nick, Model model) {
        List<Client> clients = clientRepository.findAll().stream()
                .filter(c -> (c.getTelegramNick() != null && c.getTelegramNick().toLowerCase().contains(nick.toLowerCase()))
                        || (c.getWhatsAppNick() != null && c.getWhatsAppNick().toLowerCase().contains(nick.toLowerCase())))
                .toList();

        if (clients.isEmpty()) {
            model.addAttribute("notFoundMessage", "Клиент с таким ником не найден");
        }

        model.addAttribute("clients", clients);
        return "clients";
    }

    // --- Вспомогательный метод автоформата номера ---
    private void formatPhoneNumber(Client client) {
        String digits = client.getPhoneNumber().replaceAll("\\D", "");
        if (digits.length() == 11 && digits.startsWith("8")) {
            client.setPhoneNumber(String.format("+7 (%s) %s-%s-%s",
                    digits.substring(1,4),
                    digits.substring(4,7),
                    digits.substring(7,9),
                    digits.substring(9,11)));
        }
    }
}
