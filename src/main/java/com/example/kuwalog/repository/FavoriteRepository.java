package com.example.kuwalog.repository;

import com.example.kuwalog.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserIdAndBeetleId(Long userId, Long beetleId);

    long countByBeetleId(Long beetleId);

    @org.springframework.data.jpa.repository.Query(
        "SELECT f FROM Favorite f JOIN FETCH f.beetle WHERE f.user.id = :userId ORDER BY f.createdAt DESC"
    )
    List<Favorite> findByUserIdWithBeetleOrderByCreatedAtDesc(@org.springframework.data.repository.query.Param("userId") Long userId);
}
