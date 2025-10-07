package com.application.manymilk.controller;

import com.application.manymilk.service.ClientService;
import com.application.manymilk.service.YandexDiskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.File;

@Controller
@RequiredArgsConstructor

public class HomeController {
    private final ClientService clientService;
    private final YandexDiskService diskService;


    @GetMapping("/")
    public String home(Model model) {
        long totalClients = clientService.countAllClients();
        long inactiveClients = clientService.countInactiveClients(30);
        long activeClients = totalClients - inactiveClients;

        model.addAttribute("totalClients", totalClients);
        model.addAttribute("inactiveClients", inactiveClients);
        model.addAttribute("activeClients", activeClients);

        return "home";
    }

    @PostMapping("/upload-db")
    public String uploadDatabase() {
        try {
            diskService.uploadDatabase(); // без параметров
            return "redirect:/?backup=success";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/?backup=error";
        }
    }
}
