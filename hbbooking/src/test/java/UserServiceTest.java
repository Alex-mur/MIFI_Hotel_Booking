import fun.justdevelops.hbbooking.configuration.exception.RequestException;
import fun.justdevelops.hbbooking.model.entity.User;
import fun.justdevelops.hbbooking.model.repo.UserRepo;
import fun.justdevelops.hbbooking.rest.dto.UpdateUserRequest;
import fun.justdevelops.hbbooking.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepo userRepo;
    @InjectMocks
    private UserService userService;

    @Test
    void create_ShouldSaveUser_WhenUsernameDoesNotExist() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testuser");
        savedUser.setEmail("test@example.com");

        when(userRepo.existsByUsername(user.getUsername())).thenReturn(false);
        when(userRepo.save(user)).thenReturn(savedUser);

        User result = userService.create(user);

        assertNotNull(result);
        assertEquals(savedUser.getId(), result.getId());
        assertEquals(savedUser.getUsername(), result.getUsername());
        verify(userRepo, times(1)).existsByUsername(user.getUsername());
        verify(userRepo, times(1)).save(user);
    }

    @Test
    void create_ShouldThrowException_WhenUsernameAlreadyExists() {
        User user = new User();
        user.setUsername("existinguser");
        user.setEmail("test@example.com");

        when(userRepo.existsByUsername(user.getUsername())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.create(user));

        assertEquals("User existinguser already exists", exception.getMessage());
        verify(userRepo, times(1)).existsByUsername(user.getUsername());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        List<User> expectedUsers = Arrays.asList(createUser(1L, "user1", "user1@example.com"), createUser(2L, "user2", "user2@example.com"));

        when(userRepo.findAll()).thenReturn(expectedUsers);

        List<User> result = userService.getAllUsers();

        assertEquals(expectedUsers.size(), result.size());
        assertEquals(expectedUsers, result);
        verify(userRepo, times(1)).findAll();
    }

    @Test
    void getByUsername_ShouldReturnUser_WhenUserExists() {
        String username = "testuser";
        User expectedUser = createUser(1L, username, "test@example.com");

        when(userRepo.findByUsername(username)).thenReturn(Optional.of(expectedUser));

        User result = userService.getByUsername(username);

        assertNotNull(result);
        assertEquals(expectedUser, result);
        verify(userRepo, times(1)).findByUsername(username);
    }

    @Test
    void getByUsername_ShouldThrowException_WhenUserNotFound() {
        String username = "nonexistent";
        when(userRepo.findByUsername(username)).thenReturn(Optional.empty());

        RequestException exception = assertThrows(RequestException.class, () -> userService.getByUsername(username));

        assertEquals("User not founded", exception.getMessage());
        verify(userRepo, times(1)).findByUsername(username);
    }

    @Test
    void getById_ShouldReturnUser_WhenUserExists() {
        Long userId = 1L;
        User expectedUser = createUser(userId, "testuser", "test@example.com");

        when(userRepo.findById(userId)).thenReturn(Optional.of(expectedUser));

        User result = userService.getById(userId);

        assertNotNull(result);
        assertEquals(expectedUser, result);
        verify(userRepo, times(1)).findById(userId);
    }

    @Test
    void getById_ShouldThrowException_WhenUserNotFound() {
        Long userId = 999L;
        when(userRepo.findById(userId)).thenReturn(Optional.empty());

        RequestException exception = assertThrows(RequestException.class, () -> userService.getById(userId));

        assertEquals("User not founded", exception.getMessage());
        verify(userRepo, times(1)).findById(userId);
    }

    @Test
    void userDetailsService_ShouldReturnUserDetails_WhenUserExists() {
        String username = "testuser";
        User expectedUser = createUser(1L, username, "test@example.com");
        expectedUser.setPassword("encodedPassword");

        when(userRepo.findByUsername(username)).thenReturn(Optional.of(expectedUser));

        UserDetails userDetails = userService.userDetailsService().loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals(expectedUser.getUsername(), userDetails.getUsername());
        assertEquals(expectedUser.getPassword(), userDetails.getPassword());
        verify(userRepo, times(1)).findByUsername(username);
    }

    @Test
    void getCurrentUser_ShouldReturnCurrentUser() {
        String username = "currentuser";
        User expectedUser = createUser(1L, username, "current@example.com");

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        SecurityContextHolder.setContext(securityContext);

        when(userRepo.findByUsername(username)).thenReturn(Optional.of(expectedUser));

        User result = userService.getCurrentUser();

        assertNotNull(result);
        assertEquals(expectedUser, result);
        verify(userRepo, times(1)).findByUsername(username);

        SecurityContextHolder.clearContext();
    }

    @Test
    void delete_ShouldDeleteUser_WhenUserExists() {
        Long userId = 1L;
        User user = createUser(userId, "todelete", "delete@example.com");

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userRepo).deleteById(userId);

        userService.delete(userId);

        verify(userRepo, times(1)).findById(userId);
        verify(userRepo, times(1)).deleteById(userId);
    }

    @Test
    void delete_ShouldThrowException_WhenUserNotFound() {
        Long userId = 999L;
        when(userRepo.findById(userId)).thenReturn(Optional.empty());

        RequestException exception = assertThrows(RequestException.class, () -> userService.delete(userId));

        assertEquals("User not founded", exception.getMessage());
        verify(userRepo, times(1)).findById(userId);
        verify(userRepo, never()).deleteById(anyLong());
    }

    @Test
    void update_ShouldUpdateUser_WhenUserExists() {
        Long userId = 1L;
        User existingUser = createUser(userId, "oldusername", "old@example.com");
        existingUser.setEnabled(true);

        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setUsername("newusername");
        updateRequest.setEmail("new@example.com");
        updateRequest.setEnabled(false);

        User updatedUser = createUser(userId, "newusername", "new@example.com");
        updatedUser.setEnabled(false);

        when(userRepo.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepo.save(any(User.class))).thenReturn(updatedUser);

        User result = userService.update(userId, updateRequest);

        assertNotNull(result);
        assertEquals(updateRequest.getUsername(), result.getUsername());
        assertEquals(updateRequest.getEmail(), result.getEmail());
        assertEquals(updateRequest.isEnabled(), result.isEnabled());

        verify(userRepo, times(1)).findById(userId);
        verify(userRepo, times(1)).save(existingUser);

        assertEquals(updateRequest.getUsername(), existingUser.getUsername());
        assertEquals(updateRequest.getEmail(), existingUser.getEmail());
        assertEquals(updateRequest.isEnabled(), existingUser.isEnabled());
    }

    @Test
    void update_ShouldThrowException_WhenUserNotFound() {
        Long userId = 999L;
        UpdateUserRequest updateRequest = new UpdateUserRequest();

        when(userRepo.findById(userId)).thenReturn(Optional.empty());

        RequestException exception = assertThrows(RequestException.class, () -> userService.update(userId, updateRequest));

        assertEquals("User not founded", exception.getMessage());
        verify(userRepo, times(1)).findById(userId);
        verify(userRepo, never()).save(any(User.class));
    }

    private User createUser(Long id, String username, String email) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password");
        user.setEnabled(true);
        return user;
    }
}