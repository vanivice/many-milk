package com.application.manymilk.model.db.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Имя обязательно")
    private String name;

    @NotBlank(message = "Телефон обязателен")
    @Pattern(regexp = "\\+7\\s?\\(\\d{3}\\)\\s?\\d{3}-\\d{2}-\\d{2}",
            message = "Телефон должен быть в формате +7 (999) 123-45-67")
    private String phoneNumber;

    @Column(name = "lastOrderDate")
    private LocalDate lastOrderDate;

    // Новое поле для Telegram
    @Column(name = "telegramNick")
    private String telegramNick;

    //****
    @Column(name = "secondaryPhoneNumber")
    private String secondaryPhoneNumber;
    //****

    // Новое поле для WhatsApp
    @Column(name = "whatsAppNick")
    private String whatsAppNick;

    public Client() {}

    public Client(String name, String phoneNumber, LocalDate lastOrderDate, String telegramNick, String whatsAppNick) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.lastOrderDate = lastOrderDate;
        this.telegramNick = telegramNick;
        this.whatsAppNick = whatsAppNick;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public LocalDate getLastOrderDate() { return lastOrderDate; }
    public void setLastOrderDate(LocalDate lastOrderDate) { this.lastOrderDate = lastOrderDate; }

    //****
    public String getSecondaryPhoneNumber() { return secondaryPhoneNumber; }
    public void setSecondaryPhoneNumber(String secondaryPhoneNumber) { this.secondaryPhoneNumber = secondaryPhoneNumber; }
    //****

    public String getTelegramNick() { return telegramNick; }
    public void setTelegramNick(String telegramNick) { this.telegramNick = telegramNick; }

    public String getWhatsAppNick() { return whatsAppNick; }
    public void setWhatsAppNick(String whatsAppNick) { this.whatsAppNick = whatsAppNick; }
}

