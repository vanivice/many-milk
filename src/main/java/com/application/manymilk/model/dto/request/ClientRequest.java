package com.application.manymilk.model.dto.request;

public class ClientRequest {

    private String name;
    private String phoneNumber;
    private String secondaryPhoneNumber;
    private String telegramNick;
    private String whatsAppNick;

    public ClientRequest() {}

    public ClientRequest(String name, String phoneNumber, String secondaryPhoneNumber,
                         String telegramNick, String whatsAppNick) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.secondaryPhoneNumber = secondaryPhoneNumber;
        this.telegramNick = telegramNick;
        this.whatsAppNick = whatsAppNick;
    }

    // Геттеры и сеттеры
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
}

