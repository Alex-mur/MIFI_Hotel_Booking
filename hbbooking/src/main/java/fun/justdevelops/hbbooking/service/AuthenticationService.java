package fun.justdevelops.hbbooking.service;


import fun.justdevelops.hbbooking.model.entity.Role;
import fun.justdevelops.hbbooking.model.entity.User;
import fun.justdevelops.hbbooking.rest.dto.JwtAuthenticationResponse;
import fun.justdevelops.hbbooking.rest.dto.SignInRequest;
import fun.justdevelops.hbbooking.rest.dto.SignUpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthenticationService {
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RoleService roleService;
    @Value("${hb.auth.defaultrolename}")
    private String defaultRoleName;

    @Autowired
    public AuthenticationService(UserService userService,
                                 JwtService jwtService,
                                 PasswordEncoder passwordEncoder,
                                 AuthenticationManager authenticationManager,
                                 RoleService roleService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.roleService = roleService;
    }

    public JwtAuthenticationResponse signUp(SignUpRequest request) {
        Role defaultRole = roleService.findByName(defaultRoleName);
        if (defaultRole == null) throw new RuntimeException("Default role not founded in DB");
        String encodedPassword = new BCryptPasswordEncoder().encode(request.getPassword());
        var user = new User(
                request.getUsername(),
                encodedPassword,
                List.of(defaultRole),
                request.getEmail(),
                true,
                LocalDateTime.now());
        userService.create(user);
        String jwt = jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
    }

    public JwtAuthenticationResponse signIn(SignInRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
        ));

        var user = userService
                .userDetailsService()
                .loadUserByUsername(request.getUsername());

        var jwt = jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
    }

    public long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                String username = ((UserDetails) principal).getUsername();
                User user = userService.getByUsername(username);
                return user.getId();
            } else {
                throw new RuntimeException("Cant get authenticated user id");
            }
        } else throw new RuntimeException("Cant get authenticated user id");
    }
}
