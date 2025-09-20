package com.application.manymilk.model.dto.response;

import com.application.manymilk.model.db.entity.Client;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponse {

    private Long id;
    private String name;
    private String phoneNumber;
    private String additionally;
    private String telegramNick;
    private String whatsAppNick;
    private String lastOrderDate;

    public ClientResponse(Client client) {
        this.id = client.getId();
        this.name = client.getName();
        this.phoneNumber = client.getPhoneNumber();
        this.additionally = client.getAdditionally();
        this.telegramNick = client.getTelegramNick();
        this.whatsAppNick = client.getWhatsAppNick();
        this.lastOrderDate = client.getLastOrderDate() != null ? client.getLastOrderDate().toString() : null;
    }
}
