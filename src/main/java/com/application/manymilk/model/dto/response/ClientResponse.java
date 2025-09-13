package com.application.manymilk.model.dto.response;

import com.application.manymilk.model.db.entity.Client;

public class ClientResponse {

    private Long id;
    private String name;
    private String phoneNumber;
    private String secondaryPhoneNumber;
    private String telegramNick;
    private String whatsAppNick;
    private String lastOrderDate;

    public ClientResponse(Client client) {
        this.id = client.getId();
        this.name = client.getName();
        this.phoneNumber = client.getPhoneNumber();
        this.secondaryPhoneNumber = client.getSecondaryPhoneNumber();
        this.telegramNick = client.getTelegramNick();
        this.whatsAppNick = client.getWhatsAppNick();
        this.lastOrderDate = client.getLastOrderDate() != null ? client.getLastOrderDate().toString() : null;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getSecondaryPhoneNumber() { return secondaryPhoneNumber; }
    public void setSecondaryPhoneNumber(String secondaryPhoneNumber) { this.secondaryPhoneNumber = secondaryPhoneNumber; }

    public String getTelegramNick() { return telegramNick; }
    public void setTelegramNick(String telegramNick) { this.telegramNick = telegramNick; }

    public String getWhatsAppNick() { return whatsAppNick; }
    public void setWhatsAppNick(String whatsAppNick) { this.whatsAppNick = whatsAppNick; }

    public String getLastOrderDate() { return lastOrderDate; }
    public void setLastOrderDate(String lastOrderDate) { this.lastOrderDate = lastOrderDate; }
}
