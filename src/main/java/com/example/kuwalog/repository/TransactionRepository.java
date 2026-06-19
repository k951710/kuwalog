package com.example.kuwalog.repository;

import com.example.kuwalog.entity.Beetle;
import com.example.kuwalog.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t JOIN FETCH t.fromUser JOIN FETCH t.toUser WHERE t.beetle = :beetle ORDER BY t.transferredOn DESC")
    List<Transaction> findByBeetleWithUsers(@Param("beetle") Beetle beetle);

    @Query("SELECT t FROM Transaction t JOIN FETCH t.fromUser JOIN FETCH t.toUser JOIN FETCH t.beetle WHERE t.id = :id")
    Optional<Transaction> findByIdWithUsers(@Param("id") Long id);
}
