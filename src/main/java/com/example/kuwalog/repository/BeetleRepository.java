package com.example.kuwalog.repository;

import com.example.kuwalog.entity.Beetle;
import com.example.kuwalog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BeetleRepository extends JpaRepository<Beetle, Long> {

    List<Beetle> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT b FROM Beetle b JOIN FETCH b.user LEFT JOIN FETCH b.father LEFT JOIN FETCH b.mother WHERE b.id = :id")
    Optional<Beetle> findByIdWithUser(@Param("id") Long id);

    @Query("SELECT b FROM Beetle b JOIN FETCH b.user WHERE b.sex = :sex ORDER BY b.user.username, b.name")
    List<Beetle> findBySexWithUser(@Param("sex") String sex);

    @Query("SELECT DISTINCT b FROM Beetle b JOIN FETCH b.user WHERE b.sex = :sex AND b.stage = :stage AND (b.user = :user OR EXISTS (SELECT t FROM Transaction t WHERE t.beetle = b AND t.toUser = :user)) ORDER BY b.name")
    List<Beetle> findParentCandidates(@Param("sex") String sex, @Param("stage") String stage, @Param("user") User user);

    @Query(value = "SELECT MAX(CAST(SUBSTRING(public_id FROM 4) AS INTEGER)) FROM beetles WHERE public_id LIKE CONCAT(:prefix, '%')", nativeQuery = true)
    Integer findMaxSequenceByPublicIdPrefix(@Param("prefix") String prefix);

    @Query("SELECT b FROM Beetle b JOIN FETCH b.user ORDER BY b.createdAt DESC")
    List<Beetle> findAllWithUser();

    @Query("SELECT b FROM Beetle b JOIN FETCH b.user WHERE b.species = :species AND b.sizeMm IS NOT NULL ORDER BY b.sizeMm DESC")
    List<Beetle> findRankingBySpecies(@Param("species") String species, Pageable pageable);

    @Query("SELECT b FROM Beetle b JOIN FETCH b.user WHERE b.species IN :speciesList AND b.sizeMm IS NOT NULL ORDER BY b.sizeMm DESC")
    List<Beetle> findRankingTop(@Param("speciesList") Collection<String> speciesList, Pageable pageable);
}
