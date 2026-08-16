package org.mapnaom.asset.repository;

import org.mapnaom.asset.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    List<Role> findAllByNameIn(Collection<String> names);
}
