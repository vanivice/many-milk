package com.application.manymilk.model.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientRequest {

    private String name;
    private String phoneNumber;
    private String additionally;
    private String telegramNick;
    private String whatsAppNick;
    private String lastOrderDate;
}

