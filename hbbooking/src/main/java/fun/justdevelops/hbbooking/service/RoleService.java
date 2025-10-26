package fun.justdevelops.hbbooking.service;


import fun.justdevelops.hbbooking.model.entity.Role;
import fun.justdevelops.hbbooking.model.repo.RoleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    private final RoleRepo roleRepo;

    @Autowired
    public RoleService(RoleRepo roleRepo) {
        this.roleRepo = roleRepo;
    }

    public Role get(Long id) {
        return roleRepo.getReferenceById(id);
    }

    public Role create(String name, String description) {
        if (roleRepo.existsByName(name)) {
            throw new RuntimeException("Role [" + name + "] already exists");
        }
        return roleRepo.save(new Role(name, description));
    }

    public Role findByName(String name) {
        return roleRepo.findByName(name).orElse(null);
    }

}
