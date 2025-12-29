package org.example.expert.domain.manager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "logs")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private boolean success;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Log(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    public static Log success() {
        return new Log(true, "success");
    }

    public static Log fail(String errorMessage) {
        return new Log(false, errorMessage);
    }
}
