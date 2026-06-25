package com.example.kuwalog.repository;

import com.example.kuwalog.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserIdAndBeetleId(Long userId, Long beetleId);

    long countByBeetleId(Long beetleId);

    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);
}
