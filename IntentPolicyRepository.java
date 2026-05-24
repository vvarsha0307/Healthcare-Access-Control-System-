package com.example.demo.repository;

import com.example.demo.model.IntentPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntentPolicyRepository
        extends JpaRepository<IntentPolicy, Long> {
}