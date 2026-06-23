package com.example.kuwalog.dto;

import com.example.kuwalog.entity.enums.Classification;
import com.example.kuwalog.entity.enums.Sex;
import com.example.kuwalog.entity.enums.Stage;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class BeetleForm {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private Classification classification;

    @NotNull
    private Sex sex;

    @NotNull
    private Stage stage;

    @Size(max = 20)
    private String generation;

    @Size(max = 100)
    private String locality;

    @Size(max = 7)
    private String emergenceDate;

    @Size(max = 50)
    private String breederName;

    private String description;

    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 5, fraction = 1)
    private BigDecimal sizeMm;

    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 5, fraction = 1)
    private BigDecimal weightG;

    private Long fatherId;
    private Long motherId;

    public Classification getClassification() { return classification; }
    public void setClassification(Classification classification) { this.classification = classification; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Sex getSex() { return sex; }
    public void setSex(Sex sex) { this.sex = sex; }
    public Stage getStage() { return stage; }
    public void setStage(Stage stage) { this.stage = stage; }
    public String getGeneration() { return generation; }
    public void setGeneration(String generation) { this.generation = generation; }
    public String getLocality() { return locality; }
    public void setLocality(String locality) { this.locality = locality; }
    public String getEmergenceDate() { return emergenceDate; }
    public void setEmergenceDate(String emergenceDate) { this.emergenceDate = emergenceDate; }
    public String getBreederName() { return breederName; }
    public void setBreederName(String breederName) { this.breederName = breederName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getSizeMm() { return sizeMm; }
    public void setSizeMm(BigDecimal sizeMm) { this.sizeMm = sizeMm; }
    public BigDecimal getWeightG() { return weightG; }
    public void setWeightG(BigDecimal weightG) { this.weightG = weightG; }
    public Long getFatherId() { return fatherId; }
    public void setFatherId(Long fatherId) { this.fatherId = fatherId; }
    public Long getMotherId() { return motherId; }
    public void setMotherId(Long motherId) { this.motherId = motherId; }
}
