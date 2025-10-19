package fun.justdevelops.hbauth.service;

import fun.justdevelops.hbauth.configuration.exception.RequestException;
import fun.justdevelops.hbauth.model.entity.User;
import fun.justdevelops.hbauth.model.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepo repo;

    @Autowired
    public UserService(UserRepo repo) {
        this.repo = repo;
    }

    public User save(User user) {
        return repo.save(user);
    }

    public User create(User user) {
        if (repo.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }
        return save(user);
    }

    public User getByUsername(String username) {
        return repo.findByUsername(username).orElseThrow(() -> new RequestException("Пользователь не найден"));
    }

    public UserDetailsService userDetailsService() {
        return this::getByUsername;
    }

    public User getCurrentUser() {
        // Получение имени пользователя из контекста Spring Security
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        return getByUsername(username);
    }
}
