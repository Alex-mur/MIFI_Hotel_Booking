import fun.justdevelops.hbbooking.model.entity.Role;
import fun.justdevelops.hbbooking.model.repo.RoleRepo;
import fun.justdevelops.hbbooking.service.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {
    @Mock
    private RoleRepo roleRepo;
    @InjectMocks
    private RoleService roleService;

    @Test
    void get_ShouldReturnRole_WhenRoleExists() {
        Long roleId = 1L;
        Role expectedRole = new Role("ADMIN", "Administrator");
        when(roleRepo.getReferenceById(roleId)).thenReturn(expectedRole);

        Role actualRole = roleService.get(roleId);

        assertNotNull(actualRole);
        assertEquals(expectedRole, actualRole);
        verify(roleRepo, times(1)).getReferenceById(roleId);
    }

    @Test
    void create_ShouldSaveNewRole_WhenRoleDoesNotExist() {
        String name = "USER";
        String description = "Regular user";
        when(roleRepo.existsByName(name)).thenReturn(false);
        when(roleRepo.save(any(Role.class))).thenAnswer(invocation -> {
            Role role = invocation.getArgument(0);
            return new Role(role.getName(), role.getDescription());
        });

        Role result = roleService.create(name, description);

        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(description, result.getDescription());
        verify(roleRepo, times(1)).existsByName(name);
        verify(roleRepo, times(1)).save(any(Role.class));
    }

    @Test
    void create_ShouldThrowException_WhenRoleAlreadyExists() {
        String name = "ADMIN";
        String description = "Administrator";
        when(roleRepo.existsByName(name)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> roleService.create(name, description));

        assertEquals("Role [ADMIN] already exists", exception.getMessage());
        verify(roleRepo, times(1)).existsByName(name);
        verify(roleRepo, never()).save(any(Role.class));
    }

    @Test
    void findByName_ShouldReturnRole_WhenRoleExists() {
        String roleName = "MODERATOR";
        Role expectedRole = new Role(roleName, "Moderator");
        when(roleRepo.findByName(roleName)).thenReturn(Optional.of(expectedRole));

        Role actualRole = roleService.findByName(roleName);

        assertNotNull(actualRole);
        assertEquals(expectedRole, actualRole);
        verify(roleRepo, times(1)).findByName(roleName);
    }

    @Test
    void findByName_ShouldReturnNull_WhenRoleDoesNotExist() {
        String roleName = "UNKNOWN";
        when(roleRepo.findByName(roleName)).thenReturn(Optional.empty());

        Role actualRole = roleService.findByName(roleName);

        assertNull(actualRole);
        verify(roleRepo, times(1)).findByName(roleName);
    }
}