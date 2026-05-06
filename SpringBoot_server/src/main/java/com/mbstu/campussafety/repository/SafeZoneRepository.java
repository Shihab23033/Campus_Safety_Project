package com.mbstu.campussafety.repository;

import com.mbstu.campussafety.entity.SafeZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SafeZoneRepository extends JpaRepository<SafeZone, Long> {
    Optional<SafeZone> findByName(String name);
    List<SafeZone> findAll();
}
