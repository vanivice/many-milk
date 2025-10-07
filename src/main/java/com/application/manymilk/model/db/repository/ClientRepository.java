package com.application.manymilk.model.db.repository;

import com.application.manymilk.model.db.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findByLastOrderDateBefore(LocalDate date);

    Page<Client> findAllByOrderByIdAsc(Pageable pageable);

    boolean existsByPhoneNumber(String phoneNumber);

    // кол-во клиентов
    @Query("SELECT COUNT(c) FROM Client c WHERE c.lastOrderDate < :cutoffDate")
    long countInactiveClientsSince(LocalDate cutoffDate);

    // Точное совпадение
    List<Client> findByPhoneNumber(String phoneNumber);

    // Поиск по вхождению (игнорируем нецифровые символы)
    @Query(value = "SELECT c FROM Client c WHERE " +
            "REPLACE(REPLACE(REPLACE(REPLACE(c.phoneNumber, '-', ''), ' ', ''), '(', ''), ')', '') LIKE %:digits%")
    List<Client> findByPhoneContaining(@Param("digits") String digits);

    // Поиск по нику в Telegram или WhatsApp
    @Query("SELECT c FROM Client c WHERE " +
            "LOWER(c.telegramNick) LIKE LOWER(CONCAT('%', :nickPart, '%')) OR " +
            "LOWER(c.whatsAppNick) LIKE LOWER(CONCAT('%', :nickPart, '%'))")
    List<Client> findByNickContaining(@Param("nickPart") String nickPart);
}
