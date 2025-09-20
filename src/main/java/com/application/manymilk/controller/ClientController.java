
package com.application.manymilk.controller;

import com.application.manymilk.model.dto.request.ClientRequest;
import com.application.manymilk.model.dto.response.ClientResponse;
import com.application.manymilk.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Controller
@Slf4j
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

    // Поиск клиентов по номеру в общем списке
    @GetMapping("/searchByPhone")
    public String searchByPhone(@RequestParam String phone, Model model) {

        // Проверка на пустое поле
        if (phone == null || phone.trim().isEmpty()) {
            model.addAttribute("clients", Collections.emptyList());
            model.addAttribute("notFoundMessagePhone", "Пожалуйста, введите номер телефона");
            return "clients";
        }

        // Проверка на валидный номер (только цифры, можно настроить под формат)
        if (!phone.matches("[\\d+()\\-\\s]+")) {
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

        model.addAttribute("searchType", "phone");
        model.addAttribute("searchQuery", phone);
        model.addAttribute("clients", clients);

        return "clients";
    }

    // Поиск клиентов по нику в общем списке
    @GetMapping("/searchByNick")
    public String searchByNick(@RequestParam String nick, Model model) {

        List<ClientResponse> clients = clientService.searchClientsByNick(nick);

        if (clients.isEmpty()) {
            model.addAttribute("clients", Collections.emptyList());
            model.addAttribute("notFoundMessageNick", "Клиент с таким ником не найден");
        }

        model.addAttribute("searchType", "nick");
        model.addAttribute("searchQuery", nick);
        model.addAttribute("clients", clients);

        return "clients";
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

        try {
            clientService.createClient(request);

        } catch (IllegalArgumentException e) {
            // Если возникла ошибка, передаем сообщение во view
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("clientRequest", request); // чтобы форма не очищалась
            return "add_client";
        }
        return "redirect:/clients";
    }


    // Показ формы редактирования клиента
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id,
                               @RequestParam(required = false) String searchType,
                               @RequestParam(required = false) String searchValue,
                               Model model) {
        ClientResponse client = clientService.getClientById(id);
        if (client == null) {
            return "redirect:/clients";
        }
        model.addAttribute("client", client);
        model.addAttribute("searchType", searchType);
        model.addAttribute("searchQuery", searchValue);
        return "edit_client";
    }


    // Обновление клиента
    @PostMapping("/edit/{id}")
    public String updateClient(@PathVariable Long id,
                               @Valid @ModelAttribute("client") ClientRequest request,
                               @RequestParam(required = false) String searchType,
                               @RequestParam(required = false) String searchValue,
                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) return "edit_client";

        clientService.updateClient(id, request);

        if ("nick".equals(searchType) && searchValue != null && !searchValue.isEmpty()) {
            return "redirect:/clients/searchByNick?nick=" + searchValue;
        } else if ("phone".equals(searchType) && searchValue != null && !searchValue.isEmpty()) {
            return "redirect:/clients/searchByPhone?phone=" + searchValue;
        }

        return "redirect:/clients";
    }


    // Удаление клиента
    @GetMapping("/delete/{id}")
    public String deleteClient(@PathVariable Long id, @RequestParam(required = false) String from) {
        clientService.deleteClient(id);

        if ("inactive".equals(from)) {
            return "redirect:/clients/inactive";
        } else {
            return "redirect:/clients";
        }
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
    public String updateAdditionally(@PathVariable Long id,
                                       @RequestParam String additionally,
                                       @RequestParam(required = false) String searchType,
                                       @RequestParam(required = false) String searchValue) {
        clientService.updateAdditionally(id, additionally);

        if ("nick".equals(searchType) && searchValue != null) {
            return "redirect:/clients/searchByNick?nick=" + searchValue;
        } else if ("phone".equals(searchType) && searchValue != null) {
            return "redirect:/clients/searchByPhone?phone=" + searchValue;
        }

        return "redirect:/clients";
    }

    // Дополнительный номер - удаление
    @GetMapping("/secondary-phone/delete/{id}")
    public String deleteAdditionally(@PathVariable Long id,
                                       @RequestParam(required = false) String searchType,
                                       @RequestParam(required = false) String searchValue) {
        clientService.deleteAdditionally(id);

        if ("nick".equals(searchType) && searchValue != null) {
            return "redirect:/clients/searchByNick?nick=" + searchValue;
        } else if ("phone".equals(searchType) && searchValue != null) {
            return "redirect:/clients/searchByPhone?phone=" + searchValue;
        }

        return "redirect:/clients";
    }
}
