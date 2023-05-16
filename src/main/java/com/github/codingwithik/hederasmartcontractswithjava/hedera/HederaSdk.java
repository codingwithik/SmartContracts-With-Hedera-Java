package com.github.codingwithik.hederasmartcontractswithjava.hedera;

import com.hedera.hashgraph.sdk.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class HederaSdk {

    @Value("${MasterAccountID}")
    private String masterAccountId;

    @Value("${MasterPrivateKey}")
    private String masterPrivateKey;

    @Value("${hedera.network}")
    private String hederaNetwork;


    public AccountId getMasterAccountId(){
        return AccountId.fromString(Objects.requireNonNull(masterAccountId));
    }

    public PrivateKey getMasterPrivateKey(){
        return PrivateKey.fromString(Objects.requireNonNull(masterPrivateKey));
    }

    public Client getMasterAccount(){
        Client client = Client.forName(hederaNetwork);
        client.setOperator(getMasterAccountId(), getMasterPrivateKey());
        return client;
    }

    public AccountId getAccountIdFromString(String accountId){
        return AccountId.fromString(Objects.requireNonNull(accountId));
    }

    public String currentHederaNet(){
        return  hederaNetwork;
    }

    public Client getHederaClient(String hederaNetwork, AccountId accountId, PrivateKey privateKey){
        Client client = Client.forName(hederaNetwork);
        client.setOperator(accountId, privateKey);
        return client;
    }

}
