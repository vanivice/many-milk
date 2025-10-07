package com.application.manymilk.model.db.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.validator.constraints.UniqueElements;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Имя обязательно")
    @Column(name = "name")
    private String name;

    @NotBlank(message = "Телефон обязателен")
    @Pattern(regexp = "\\+7\\s?\\(\\d{3}\\)\\s?\\d{3}-\\d{2}-\\d{2}",
            message = "Телефон должен быть в формате +7 (999) 123-45-67")
    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "additionally")
    private String additionally;

    @Column(name = "last_order_date")
    private LocalDate lastOrderDate;

    @Column(name = "telegram_nick")
    private String telegramNick;

    @Column(name = "whats_app_nick")
    private String whatsAppNick;
}

