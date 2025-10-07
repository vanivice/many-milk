
package com.application.manymilk.controller;

import com.application.manymilk.model.dto.request.ClientRequest;
import com.application.manymilk.model.dto.response.ClientResponse;
import com.application.manymilk.service.ClientService;
import com.application.manymilk.util.PaginationUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@Slf4j
@RequestMapping("/clients")
@RequiredArgsConstructor

public class ClientController {

    private final ClientService clientService;

    // Список всех клиентов
    @GetMapping
    public String listClients(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "21") int size,
                              Model model) {
        Pageable pageable = PaginationUtil.createPageable(page, size, true);
        Page<ClientResponse> clientsPage = clientService.getAllClients(pageable);

        model.addAttribute("clients", clientsPage);
        model.addAttribute("isSearch", false);
        model.addAttribute("baseUrl", "/clients");

        return "clients";
    }

    // Поиск клиентов по номеру в общем списке
    @GetMapping("/searchByPhone")
    public String searchByPhone(@RequestParam String phone, Model model) {

        if (phone == null || phone.trim().isEmpty()) {
            return handleEmptySearch(model, "phone", "Пожалуйста, введите номер телефона");
        }

        if (!phone.matches("[\\d+()\\-\\s]+")) {
            return handleEmptySearch(model, "phone", "Некорректный номер телефона");
        }

        // Используем сервисный метод, который уже ищет по базе
        List<ClientResponse> clients = clientService.searchClientsByPhone(phone);

        model.addAttribute("clients", clients);
        model.addAttribute("isSearch", true);
        model.addAttribute("searchType", "phone");
        model.addAttribute("searchQuery", phone);

        // Унифицированное сообщение о том, что клиент не найден
        if (clients.isEmpty()) {
            model.addAttribute("notFoundMessagePhone", "Клиент с таким номером не найден");
        }

        return "clients";
    }

    // Поиск клиентов по нику
    @GetMapping("/searchByNick")
    public String searchByNick(@RequestParam String nick, Model model) {

        if (nick == null || nick.trim().isEmpty()) {
            return handleEmptySearch(model, "nick", "Пожалуйста, введите ник");
        }

        // Используем новый метод репозитория в сервисе
        List<ClientResponse> clients = clientService.searchClientsByNick(nick);

        model.addAttribute("clients", clients);
        model.addAttribute("isSearch", true);
        model.addAttribute("searchType", "nick");
        model.addAttribute("searchQuery", nick);

        // Унифицированное сообщение о том, что ник не найден
        if (clients.isEmpty()) {
            model.addAttribute("notFoundMessageNick", "Клиент с таким ником не найден");
        }

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
                               @RequestParam(required = false) Integer page,
                               Model model) {
        ClientResponse client = clientService.getClientById(id);
        if (client == null) {
            return "redirect:/clients";
        }
        model.addAttribute("client", client);
        model.addAttribute("searchType", searchType);
        model.addAttribute("searchQuery", searchValue);
        model.addAttribute("currentPage", page);
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

        return redirectToSearch(searchType, searchValue, "/clients");
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

        return redirectToSearch(searchType, searchValue, "/clients");
    }

    // Дополнительный номер - удаление
    @GetMapping("/secondary-phone/delete/{id}")
    public String deleteAdditionally(@PathVariable Long id,
                                       @RequestParam(required = false) String searchType,
                                       @RequestParam(required = false) String searchValue) {
        clientService.deleteAdditionally(id);

        return redirectToSearch(searchType, searchValue, "/clients");
    }

    // метод для проверки поиска
    private String handleEmptySearch(Model model, String type, String message) {
        model.addAttribute("clients", new ArrayList<>());
        model.addAttribute("isSearch", true);
        model.addAttribute("searchType", type);
        model.addAttribute("searchQuery", "");
        model.addAttribute("notFoundMessage" + capitalize(type), message);
        return "clients";
    }

    // метод редиректа
    private String redirectToSearch(String searchType, String searchValue, String defaultRedirect) {
        if ("nick".equals(searchType) && searchValue != null && !searchValue.isEmpty()) {
            return "redirect:/clients/searchByNick?nick=" + searchValue;
        } else if ("phone".equals(searchType) && searchValue != null && !searchValue.isEmpty()) {
            return "redirect:/clients/searchByPhone?phone=" + searchValue;
        }
        return "redirect:" + defaultRedirect;
    }

    // утилита для капитализации первой букы
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0,1).toUpperCase() + str.substring(1);
    }
}
