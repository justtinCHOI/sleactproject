package com.unitekndt.mqnavigator.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "mentions")
public class Mention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = true)
    private Integer chatId;

    @ManyToOne
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    public enum Category {
        CHAT, DM, SYSTEM
    }

    // Getters and Setters
}