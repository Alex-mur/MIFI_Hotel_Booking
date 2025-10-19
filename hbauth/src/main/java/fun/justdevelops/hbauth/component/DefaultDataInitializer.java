package fun.justdevelops.hbauth.component;


import fun.justdevelops.hbauth.model.entity.Role;
import fun.justdevelops.hbauth.model.entity.User;
import fun.justdevelops.hbauth.model.repo.RoleRepo;
import fun.justdevelops.hbauth.model.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.hibernate.internal.util.collections.CollectionHelper.listOf;

@Component
public class DefaultDataInitializer {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DefaultDataInitializer(UserRepo userRepo, RoleRepo roleRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // Создание дефолтных ролей и пользователей если их нет в бд
    @EventListener(ContextRefreshedEvent.class)
    public void initDefaultRolesAndUsers() {
        // Создание ролей
        Role adminRole;
        Role userRole;
        if (roleRepo.count() == 0) {
            adminRole = new Role("ADMIN", "Default role with admin privileges");
            userRole = new Role("USER", "Default role with user privileges");
            roleRepo.save(adminRole);
            roleRepo.save(userRole);
        } else {
            userRole = roleRepo.findByName("USER").orElseThrow(() -> new RuntimeException("Role USER not exist"));
            adminRole = roleRepo.findByName("ADMIN").orElseThrow(() -> new RuntimeException("Role ADMIN not exist"));
        }

        if (userRepo.count() > 0) {
            return;
        }

        // Создание пользователей
        User u1 = new User("admin", passwordEncoder.encode("12345"), listOf(adminRole), "admin@example.com", true, LocalDateTime.now());
        User u2 = new User("user1", passwordEncoder.encode("123"), listOf(userRole), "user1@example.com", true, LocalDateTime.now());
        User u3 = new User("user2", passwordEncoder.encode("123"), listOf(userRole), "user2@example.com", true, LocalDateTime.now());

        userRepo.saveAll(listOf(u1, u2, u3));
    }

}
