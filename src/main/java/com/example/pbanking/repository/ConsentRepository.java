package com.example.pbanking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.pbanking.model.Consent;

public interface ConsentRepository extends JpaRepository<Consent, String> {
    
}
