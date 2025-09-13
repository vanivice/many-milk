
package com.application.manymilk.controller;

import com.application.manymilk.model.dto.request.ClientRequest;
import com.application.manymilk.model.dto.response.ClientResponse;
import com.application.manymilk.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/clients")
@RequiredArgsConstructor

public class ClientController {

    private final ClientService clientService;

    // Список всех клиентов
    @GetMapping
    public String listClients(Model model) {
        List<ClientResponse> clients = clientService.getAllClients();
        model.addAttribute("clients", clients);
        return "clients";
    }

    // Поиск клиентов по номеру
    @GetMapping("/searchByPhone")
    public String searchByPhone(@RequestParam String phone, Model model) {
        // Проверка на пустое поле
        if (phone == null || phone.trim().isEmpty()) {
            model.addAttribute("clients", Collections.emptyList());
            model.addAttribute("notFoundMessagePhone", "Пожалуйста, введите номер телефона");
            return "clients";
        }

        // Проверка на валидный номер (только цифры, можно настроить под формат)
        if (!phone.matches("\\d+")) {
            model.addAttribute("clients", Collections.emptyList());
            model.addAttribute("notFoundMessagePhone", "Некорректный номер телефона");
            return "clients";
        }

        // Поиск клиентов
        List<ClientResponse> clients = clientService.searchClientsByPhone(phone);

        if (clients.isEmpty()) {
            model.addAttribute("clients", Collections.emptyList());
            model.addAttribute("notFoundMessagePhone", "Клиент с таким номером не найден");
            return "clients";
        }

        model.addAttribute("clients", clients);
        return "clients";
    }

    // Поиск клиентов по нику Telegram/WhatsApp
    @GetMapping("/searchByNick")
    public String searchByNick(@RequestParam String nick, Model model) {
        List<ClientResponse> clients = clientService.searchClientsByNick(nick);
        if (clients.isEmpty()) {
            model.addAttribute("notFoundMessageNick", "Клиент с таким ником не найден");
        }
        model.addAttribute("clients", clients);
        return "clients";
    }

    @GetMapping("/inactive/searchByPhone")
    public String searchByPhoneInactive(@RequestParam String phone, Model model) {
        // Проверка на пустое поле
        if (phone == null || phone.trim().isEmpty()) {
            model.addAttribute("clients", Collections.emptyList());
            model.addAttribute("notFoundMessagePhone", "Пожалуйста, введите номер телефона");
            return "inactive_clients";
        }

        // Проверка на валидный номер (только цифры, можно настроить под формат)
        if (!phone.matches("\\d+")) {
            model.addAttribute("clients", Collections.emptyList());
            model.addAttribute("notFoundMessagePhone", "Некорректный номер телефона");
            return "inactive_clients";
        }

        // Поиск клиентов
        List<ClientResponse> clients = clientService.searchClientsByPhone(phone);

        if (clients.isEmpty()) {
            model.addAttribute("clients", Collections.emptyList());
            model.addAttribute("notFoundMessagePhone", "Клиент с таким номером не найден");
            return "inactive_clients";
        }

        model.addAttribute("clients", clients);
        return "inactive_clients";
    }

    // Поиск клиентов по нику Telegram/WhatsApp в неактивных
    @GetMapping("/inactive/searchByNick")
    public String searchByNickInactive(@RequestParam String nick, Model model) {
        List<ClientResponse> clients = clientService.searchClientsByNick(nick);

        if (clients.isEmpty()) {
            model.addAttribute("notFoundMessageNick", "Клиент с таким ником не найден");
        }

        model.addAttribute("clients", clients);
        return "inactive_clients";
    }

    // Показ формы добавления клиента
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("client", new ClientRequest());
        return "add_client";
    }

    // Добавление клиента
    @PostMapping("/add")
    public String addClient(@Valid @ModelAttribute("client") ClientRequest request,
                            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) return "add_client";

        clientService.createClient(request);
        return "redirect:/clients";
    }

    // Показ формы редактирования клиента
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        ClientResponse client = clientService.getClientById(id);
        if (client == null) {
            return "redirect:/clients";
        }
        model.addAttribute("client", client);
        return "edit_client";
    }

    // Обновление клиента
    @PostMapping("/edit/{id}")
    public String updateClient(@PathVariable Long id,
                               @Valid @ModelAttribute("client") ClientRequest request,
                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) return "edit_client";

        clientService.updateClient(id, request);
        return "redirect:/clients";
    }

    // Удаление клиента
    @GetMapping("/delete/{id}")
    public String deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return "redirect:/clients/inactive";
    }

    // Отметка "сделал заказ сегодня"
    @PostMapping("/order-today/{id}")
    public String markOrderToday(@PathVariable Long id,
                                 @RequestHeader(value = "referer", required = false) String referer) {
        clientService.markOrderToday(id);
        return referer != null ? "redirect:" + referer : "redirect:/clients";
    }

    // Список неактивных клиентов
    @GetMapping("/inactive")
    public String inactiveClients(Model model) {
        List<ClientResponse> inactive = clientService.getInactiveClients(30);
        model.addAttribute("clients", inactive);
        return "inactive_clients";
    }

    // Дополнительный номер - обновление
    @PostMapping("/secondary-phone/{id}")
    public String updateSecondaryPhone(@PathVariable Long id,
                                       @RequestParam String secondaryPhoneNumber) {
        clientService.updateSecondaryPhone(id, secondaryPhoneNumber);
        return "redirect:/clients";
    }

    // Дополнительный номер - удаление
    @GetMapping("/secondary-phone/delete/{id}")
    public String deleteSecondaryPhone(@PathVariable Long id) {
        clientService.deleteSecondaryPhone(id);
        return "redirect:/clients";
    }
}
