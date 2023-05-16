package com.github.codingwithik.hederasmartcontractswithjava.services;

import com.github.codingwithik.hederasmartcontractswithjava.contracts.HelloHederaContract;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HelloHederaService {

    private final HelloHederaContract helloHederaContract;

    public String getMessage(){
        return helloHederaContract.callSmartContractGetFunction();
    }

    public void setMessage(String message){
        helloHederaContract.callSmartContractSetFunction(message);
    }
}
