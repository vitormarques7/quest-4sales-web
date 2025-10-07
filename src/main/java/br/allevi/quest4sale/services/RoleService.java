package br.allevi.quest4sale.services;

import br.allevi.quest4sale.entities.Role;
import br.allevi.quest4sale.repositories.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public Role getById(UUID id) {
        return roleRepository.findById(id).orElseThrow();
    }

    @Transactional(readOnly = true)
    public Role getByName(String name) {
        return roleRepository.findByName(name).orElseThrow();
    }

    @Transactional
    public Role create(Role role) {
        return roleRepository.save(role);
    }

    @Transactional
    public Role update(Role role) {
        return roleRepository.save(role);
    }

    @Transactional
    public void delete(UUID id) {
        roleRepository.deleteById(id);
    }
}


