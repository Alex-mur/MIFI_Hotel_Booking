package fun.justdevelops.hbauth.service;

import fun.justdevelops.hbauth.model.entity.Role;
import fun.justdevelops.hbauth.model.entity.User;
import fun.justdevelops.hbauth.rest.dto.JwtAuthenticationResponse;
import fun.justdevelops.hbauth.rest.dto.SignInRequest;
import fun.justdevelops.hbauth.rest.dto.SignUpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    @Value("${si.auth.defaultrolename}")
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
        if (defaultRole == null) throw new RuntimeException("В БД не найдена роль по-умолчанию для новых пользователей");
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

}
