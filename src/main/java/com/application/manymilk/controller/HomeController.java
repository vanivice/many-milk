package com.application.manymilk.controller;

import com.application.manymilk.model.db.repository.ClientRepository;
import com.application.manymilk.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor

public class HomeController {
    private final ClientService clientService;

    @GetMapping("/")
    public String home(Model model) {
        long totalClients = clientService.getAllClients().stream().count();
        long inactiveClients = clientService.getInactiveClients(30).stream().count();
        long activeClients = totalClients - inactiveClients;

        model.addAttribute("totalClients", totalClients);
        model.addAttribute("inactiveClients", inactiveClients);
        model.addAttribute("activeClients", activeClients);

        return "home";
    }
}
