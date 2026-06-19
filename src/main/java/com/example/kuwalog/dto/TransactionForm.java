package com.example.kuwalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class TransactionForm {

    @NotBlank
    private String toUsername;

    @NotNull
    private LocalDate transferredOn;

    public String getToUsername() { return toUsername; }
    public void setToUsername(String toUsername) { this.toUsername = toUsername; }
    public LocalDate getTransferredOn() { return transferredOn; }
    public void setTransferredOn(LocalDate transferredOn) { this.transferredOn = transferredOn; }
}
