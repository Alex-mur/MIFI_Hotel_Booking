package fun.justdevelops.hbmanagement.configuration.security.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class User implements UserDetails {
    private long id;
    @Column(nullable = false)
    private String username;

    @JsonIgnore
    private String password;

    private List<String> roles = new ArrayList<>();

    private String email;

    private Boolean isEnabled = true;

    private LocalDateTime createdAt;

    public User() {
    }

    public User(String username, String password, List<String> roles, String email, Boolean isEnabled, LocalDateTime createdAt) {
        this.username = username;
        this.password = password;
        this.roles = roles;
        this.email = email;
        this.isEnabled = isEnabled;
        this.createdAt = createdAt;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    public void setEmail() {
        this.email = email;
    }

    public void setEnabled(Boolean enabled) {
        isEnabled = enabled;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
