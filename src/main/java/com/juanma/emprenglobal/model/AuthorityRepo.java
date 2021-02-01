package com.juanma.emprenglobal.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorityRepo extends JpaRepository<Authority, Long> {
    Optional<Authority> findByAname(String aname);
}
