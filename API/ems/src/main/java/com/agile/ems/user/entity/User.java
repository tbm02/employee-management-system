package com.agile.ems.user.entity;

import com.agile.ems.user.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "emp_id", nullable = false, unique = true, length = 20)
    private String empId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Role role;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(name = "first_login", nullable = false)
    private Boolean firstLogin;

    @Column(name = "is_password_updated", nullable = false)
    private Boolean passwordUpdated;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "user")
    private UserDetails userDetails;

    @PrePersist
    void prePersist() {
        if (enabled == null) {
            enabled = true;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (firstLogin == null) {
            firstLogin = true;
        }
        if (passwordUpdated == null) {
            passwordUpdated = false;
        }
    }
}
