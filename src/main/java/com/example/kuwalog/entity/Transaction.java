package com.example.kuwalog.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beetle_id", nullable = false)
    private Beetle beetle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;

    @Column(name = "transferred_on", nullable = false)
    private LocalDate transferredOn;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Beetle getBeetle() { return beetle; }
    public void setBeetle(Beetle beetle) { this.beetle = beetle; }
    public User getFromUser() { return fromUser; }
    public void setFromUser(User fromUser) { this.fromUser = fromUser; }
    public User getToUser() { return toUser; }
    public void setToUser(User toUser) { this.toUser = toUser; }
    public LocalDate getTransferredOn() { return transferredOn; }
    public void setTransferredOn(LocalDate transferredOn) { this.transferredOn = transferredOn; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
