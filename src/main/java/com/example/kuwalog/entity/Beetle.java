package com.example.kuwalog.entity;

import com.example.kuwalog.entity.enums.Sex;
import com.example.kuwalog.entity.enums.Stage;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "beetles")
public class Beetle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "father_id")
    private Beetle father;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mother_id")
    private Beetle mother;

    @Column(nullable = false, length = 100)
    private String name;

    // DB値はラベル文字列（'オス'等）で保存するためStringとして扱い、enumはService層で変換する
    @Column(nullable = false, length = 10)
    private String sex;

    @Column(length = 20)
    private String generation;

    @Column(length = 100)
    private String locality;

    @Column(name = "emergence_date", length = 7)
    private String emergenceDate;

    @Column(name = "classification", length = 20)
    private String classification;

    @Column(name = "breeder_name", length = 50)
    private String breederName;

    @Column(nullable = false, length = 10)
    private String stage;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "size_mm", precision = 6, scale = 1)
    private java.math.BigDecimal sizeMm;

    @Column(name = "weight_g", precision = 6, scale = 1)
    private java.math.BigDecimal weightG;

    @Column(name = "public_id", length = 9, unique = true)
    private String publicId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Beetle getFather() { return father; }
    public void setFather(Beetle father) { this.father = father; }
    public Beetle getMother() { return mother; }
    public void setMother(Beetle mother) { this.mother = mother; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }
    public String getGeneration() { return generation; }
    public void setGeneration(String generation) { this.generation = generation; }
    public String getLocality() { return locality; }
    public void setLocality(String locality) { this.locality = locality; }
    public String getEmergenceDate() { return emergenceDate; }
    public void setEmergenceDate(String emergenceDate) { this.emergenceDate = emergenceDate; }
    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }
    public String getBreederName() { return breederName; }
    public void setBreederName(String breederName) { this.breederName = breederName; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public java.math.BigDecimal getSizeMm() { return sizeMm; }
    public void setSizeMm(java.math.BigDecimal sizeMm) { this.sizeMm = sizeMm; }
    public java.math.BigDecimal getWeightG() { return weightG; }
    public void setWeightG(java.math.BigDecimal weightG) { this.weightG = weightG; }
    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
