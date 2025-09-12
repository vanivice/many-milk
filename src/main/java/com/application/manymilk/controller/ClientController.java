package com.application.manymilk.controller;

import com.application.manymilk.model.db.entity.Client;
import com.application.manymilk.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    // --- Список всех клиентов ---
    @GetMapping
    public String listClients(Model model) {
        model.addAttribute("clients", clientService.getAllClients());
        return "clients";
    }

    // --- Поиск клиентов по номеру телефона ---
    @GetMapping("/search")
    public String searchClients(@RequestParam("phone") String phone, Model model) {
        String last4 = null;
        if (phone != null) {
            // Берём только цифры
            String digits = phone.replaceAll("\\D", "");
            if (digits.length() >= 4) {
                last4 = digits.substring(digits.length() - 4); // последние 4 цифры
            }
        }

        List<Client> clients;
        if (last4 != null) {
            clients = clientService.searchClientsByLast4Digits(last4);
        } else {
            clients = clientService.getAllClients();
        }

        model.addAttribute("clients", clients);

        if ((clients == null || clients.isEmpty()) && last4 != null) {
            model.addAttribute("notFoundMessage", "Клиент с такими последними 4 цифрами не найден");
        }

        return "clients";
    }

    // --- Форма добавления нового клиента ---
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("client", new Client());
        return "add_client";
    }

    // --- Сохранение нового клиента ---
    @PostMapping("/add")
    public String addClient(@Valid @ModelAttribute("client") Client client,
                            BindingResult result) {
        if (result.hasErrors()) {
            return "add_client";
        }
        clientService.saveClient(client);
        return "redirect:/clients";
    }

    // --- Форма редактирования клиента ---
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Client client = clientService.getClientById(id);
        if (client == null) {
            return "redirect:/clients";
        }
        model.addAttribute("client", client);
        return "edit_client";
    }

    // --- Сохранение изменений клиента ---
    @PostMapping("/edit/{id}")
    public String editClient(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("client") Client client,
                             BindingResult result) {
        if (result.hasErrors()) {
            return "edit_client";
        }
        clientService.updateClient(id, client);
        return "redirect:/clients";
    }

    // --- Удаление клиента ---
    @GetMapping("/delete/{id}")
    public String deleteClient(@PathVariable("id") Long id) {
        clientService.deleteClient(id);
        return "redirect:/clients";
    }

    // --- Список неактивных клиентов ---
    @GetMapping("/inactive")
    public String inactiveClients(Model model) {
        List<Client> inactive = clientService.getInactiveClients(30);
        model.addAttribute("clients", inactive);
        return "inactive_clients";
    }

    // --- Отметить, что клиент сделал заказ сегодня ---
    @PostMapping("/order-today/{id}")
    public String markOrderToday(@PathVariable("id") Long id, @RequestHeader(value = "referer", required = false) String referer) {
        Client client = clientService.getClientById(id);
        if (client != null) {
            client.setLastOrderDate(LocalDate.now());
            clientService.saveClient(client);
        }
        // Редирект обратно на ту страницу, с которой пришёл запрос
        if (referer != null) {
            return "redirect:" + referer;
        }
        return "redirect:/clients";
    }
}