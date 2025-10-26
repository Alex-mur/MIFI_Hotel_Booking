package fun.justdevelops.hbbooking.service;

import fun.justdevelops.hbbooking.configuration.exception.RequestException;
import fun.justdevelops.hbbooking.model.entity.User;
import fun.justdevelops.hbbooking.model.repo.UserRepo;
import fun.justdevelops.hbbooking.rest.dto.UpdateUserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepo repo;

    @Autowired
    public UserService(UserRepo repo) {
        this.repo = repo;
    }

    public User create(User user) {
        if (repo.existsByUsername(user.getUsername())) {
            throw new RuntimeException("User " + user.getUsername() + " already exists");
        }
        return repo.save(user);
    }

    public List<User> getAllUsers() {
        return repo.findAll();
    }

    public User getByUsername(String username) {
        return repo.findByUsername(username).orElseThrow(() -> new RequestException("User not founded"));
    }

    public User getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new RequestException("User not founded"));
    }

    public UserDetailsService userDetailsService() {
        return this::getByUsername;
    }

    public User getCurrentUser() {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        return getByUsername(username);
    }

    public void delete(Long id) {
        User user = getById(id);
        repo.deleteById(id);
    }

    public User update(Long id, UpdateUserRequest request) {
        User user = getById(id);
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setEnabled(request.isEnabled());
        return repo.save(user);
    }
}
