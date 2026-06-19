package com.example.kuwalog.dto;

import com.example.kuwalog.entity.enums.Sex;
import com.example.kuwalog.entity.enums.Stage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class BeetleForm {

    @NotBlank
    @Size(max = 100)
    private String name;

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

    private String description;

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
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
