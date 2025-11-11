package com.example.pbanking.user;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.example.pbanking.common.enums.UserStatus;
import com.example.pbanking.consent.Credentials;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    public User(String username, String password, UserStatus status) {
        this.username = username;
        this.password = password;
        this.status = status;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Nonnull
    @Column(unique = true)
    private String username;

    @Nonnull
    private String password;
    
    private String name = null;
    
    private String surname = null;
    
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.DEFAULT;
    
    private Instant statusExpireDate = null;

    @OneToMany(mappedBy = "user")
    @JsonManagedReference
    private List<Credentials> consents;
}
