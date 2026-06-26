package com.example.kuwalog.repository;

import com.example.kuwalog.entity.Beetle;
import com.example.kuwalog.entity.BeetleImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BeetleImageRepository extends JpaRepository<BeetleImage, Long> {

    List<BeetleImage> findByBeetleOrderBySortOrderAsc(Beetle beetle);

    int countByBeetle(Beetle beetle);

    Optional<BeetleImage> findFirstByBeetleOrderBySortOrderAsc(Beetle beetle);

    @Modifying
    @Query("UPDATE BeetleImage bi SET bi.primary = false WHERE bi.beetle = :beetle")
    void clearPrimaryByBeetle(@Param("beetle") Beetle beetle);
}
