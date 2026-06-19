package com.example.kuwalog.repository;

import com.example.kuwalog.entity.Beetle;
import com.example.kuwalog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BeetleRepository extends JpaRepository<Beetle, Long> {

    List<Beetle> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT b FROM Beetle b JOIN FETCH b.user WHERE b.id = :id")
    Optional<Beetle> findByIdWithUser(@Param("id") Long id);
}
