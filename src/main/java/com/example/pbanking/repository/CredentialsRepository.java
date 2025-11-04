package com.example.pbanking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.pbanking.model.Credentials;

@Repository
public interface CredentialsRepository extends JpaRepository<Credentials, String> {

}
