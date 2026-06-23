package com.example.kuwalog.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "used_beetle_public_ids")
public class UsedBeetlePublicId {

    @Id
    @Column(name = "public_id", length = 9)
    private String publicId;

    public UsedBeetlePublicId() {}

    public UsedBeetlePublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getPublicId() { return publicId; }
}
