package com.example.kuwalog.repository;

import com.example.kuwalog.entity.UsedBeetlePublicId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsedBeetlePublicIdRepository extends JpaRepository<UsedBeetlePublicId, String> {

    @Query(value = "SELECT MAX(CAST(SUBSTRING(public_id FROM 4) AS INTEGER)) FROM used_beetle_public_ids WHERE public_id LIKE CONCAT(:prefix, '%')", nativeQuery = true)
    Integer findMaxSequenceByPrefix(@Param("prefix") String prefix);
}
