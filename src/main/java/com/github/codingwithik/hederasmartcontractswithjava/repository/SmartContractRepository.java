package com.github.codingwithik.hederasmartcontractswithjava.repository;

import com.github.codingwithik.hederasmartcontractswithjava.models.SmartContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmartContractRepository extends JpaRepository<SmartContract, Integer> {

    SmartContract findByContractName(String name);
}
