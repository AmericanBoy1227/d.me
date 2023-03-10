package com.d.me.data.service;

import com.d.me.data.entity.SystemUser;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class SystemUserService {

    private final SystemUserRepository repository;

    public SystemUserService(SystemUserRepository repository) {
        this.repository = repository;
    }

    public Optional<SystemUser> get(Long id) {
        return repository.findById(id);
    }

    public SystemUser update(SystemUser entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<SystemUser> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<SystemUser> list(Pageable pageable, Specification<SystemUser> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
