package com.application.manymilk.model.db.repository;

import com.application.manymilk.model.db.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByLastOrderDateBefore(LocalDate date);

    List<Client> findAllByOrderByIdAsc();

    // Поиск по последним 4 цифрам номера телефона
    @Query("SELECT c FROM Client c WHERE FUNCTION('RIGHT', REPLACE(c.phoneNumber, '\\D',''), 4) = :lastDigits")
    List<Client> findByLast4Digits(@Param("lastDigits") String lastDigits);
}
