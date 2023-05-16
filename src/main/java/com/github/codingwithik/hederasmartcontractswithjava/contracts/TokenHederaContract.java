package com.github.codingwithik.hederasmartcontractswithjava.contracts;

import com.github.codingwithik.hederasmartcontractswithjava.dto.CreateTokenDto;
import com.github.codingwithik.hederasmartcontractswithjava.dto.UserDto;
import com.github.codingwithik.hederasmartcontractswithjava.hedera.HederaSdk;
import com.github.codingwithik.hederasmartcontractswithjava.models.SmartContract;
import com.github.codingwithik.hederasmartcontractswithjava.repository.SmartContractRepository;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenHederaContract {

    private final HederaSdk hederaSdk;
    private final SmartContractRepository contractRepository;

    public FileId storeContract() throws ReceiptStatusException, PrecheckStatusException, TimeoutException {
        log.info("Storing contract ...");
        SmartContract contract = contractRepository.findByContractName("Cbdc_Contract");
        if(contract != null){
            return FileId.fromString(contract.getFileId());
        }
        //Import the compiled contract from the HelloHedera.json file
        Gson gson = new Gson();
        JsonObject jsonObject;

        InputStream jsonStream = TokenHederaContract.class.getClassLoader().getResourceAsStream("smartcontracts/bytecode/CBDC.json");
        jsonObject = gson.fromJson(new InputStreamReader(jsonStream, StandardCharsets.UTF_8), JsonObject.class);

        //Store the "object" field from the HelloHedera.json file as hex-encoded bytecode
        String object = jsonObject.getAsJsonObject("data").getAsJsonObject("bytecode").get("object").getAsString();
        byte[] bytecode = object.getBytes(StandardCharsets.UTF_8);

        //Create a file on Hedera and store the hex-encoded bytecode
        FileCreateTransaction fileCreateTx = new FileCreateTransaction()
                .setKeys(hederaSdk.getMasterPrivateKey())
                //Set the bytecode of the contract
                .setContents(bytecode);

        //Submit the file to the Hedera test network signing with the transaction fee payer key specified with the client
        TransactionResponse submitTx = fileCreateTx.execute(hederaSdk.getMasterAccount());

        //Get the receipt of the file create transaction
        TransactionReceipt fileReceipt = submitTx.getReceipt(hederaSdk.getMasterAccount());

        //Get the file ID from the receipt
        FileId bytecodeFileId = fileReceipt.fileId;

        //Log the file ID
        System.out.println("The smart contract bytecode file ID is " +bytecodeFileId);

        // save fileId
        SmartContract newContract = new SmartContract();
        newContract.setFileId(bytecodeFileId.toString());
        newContract.setContractName("Cbdc_Contract");

        contractRepository.save(newContract);

        return bytecodeFileId;

    }


    public ContractId deployContract() throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        log.info("Deploying CBDC contract ...");
        SmartContract contract = contractRepository.findByContractName("Cbdc_Contract");
        if(contract != null && contract.getContractId() != null){
            return ContractId.fromString(contract.getContractId());
        }


        // Instantiate the contract instance
        ContractCreateTransaction contractTx = new ContractCreateTransaction()
                //Set the file ID of the Hedera file storing the bytecode
                .setBytecodeFileId(storeContract())
                //Set the gas to instantiate the contract
                .setGas(200_000);

        //Submit the transaction to the Hedera test network
        TransactionResponse contractResponse = contractTx.execute(hederaSdk.getMasterAccount());

        //Get the receipt of the file create transaction
        TransactionReceipt contractReceipt = contractResponse.getReceipt(hederaSdk.getMasterAccount());

        //Get the smart contract ID
        ContractId newContractId = contractReceipt.contractId;

        //Log the smart contract ID
        System.out.println("The smart contract ID is " + newContractId);

        SmartContract contractInDb = contractRepository.findByContractName("Cbdc_Contract");
        if(contract != null){
            contractInDb.setContractId(newContractId.toString());
            contractRepository.save(contractInDb);
        }

        return newContractId;
    }

    public Integer callSmartContractCreateUserFunction(UserDto userDto) {

        try {

            // Calls a function of the smart contract
            ContractCallQuery contractQuery = new ContractCallQuery()
                    //Set the gas for the query
                    .setGas(100000)
                    //Set the contract ID to return the request for
                    .setContractId(deployContract())
                    //Set the function of the contract to call
                    .setFunction("addUser", new ContractFunctionParameters()
                            .addString(userDto.regulatorName())
                            .addString(userDto.role())
                            .addAddress(hederaSdk.getMasterAccountId().toSolidityAddress())
                            .addString(userDto.country()))

                    //Set the query payment for the node returning the request
                    //This value must cover the cost of the request otherwise will fail
                    .setQueryPayment(new Hbar(2));

            //Submit to a Hedera network
            ContractFunctionResult getMessage = contractQuery.execute(hederaSdk.getMasterAccount());
            //Get the message
            Integer result = getMessage.getUint32(0);

            //Log the message
            System.out.println("User Id: " + result);
            return result;
        }catch(PrecheckStatusException | TimeoutException | ReceiptStatusException ex){
            ex.printStackTrace();
            return null;
        }

    }

    public String callSmartContractCreateTokenFunction(CreateTokenDto tokenDto) {

        try {

            // Calls a function of the smart contract
            ContractCallQuery contractQuery = new ContractCallQuery()
                    //Set the gas for the query
                    .setGas(100000)
                    //Set the contract ID to return the request for
                    .setContractId(deployContract())
                    //Set the function of the contract to call
                    .setFunction("createToken")
                    //Set the query payment for the node returning the request
                    //This value must cover the cost of the request otherwise will fail
                    .setQueryPayment(new Hbar(2));

            //Submit to a Hedera network
            ContractFunctionResult getMessage = contractQuery.execute(hederaSdk.getMasterAccount());
            //Get the message
            String message = getMessage.getString(0);

            //Log the message
            System.out.println("The contract message: " + message);
            return message;
        }catch(PrecheckStatusException | TimeoutException | ReceiptStatusException ex){
            ex.printStackTrace();
            return null;
        }

    }

    public String callSmartContractMintTokenFunction() {

        try {

            // Calls a function of the smart contract
            ContractCallQuery contractQuery = new ContractCallQuery()
                    //Set the gas for the query
                    .setGas(100000)
                    //Set the contract ID to return the request for
                    .setContractId(deployContract())
                    //Set the function of the contract to call
                    .setFunction("mint" )
                    //Set the query payment for the node returning the request
                    //This value must cover the cost of the request otherwise will fail
                    .setQueryPayment(new Hbar(2));

            //Submit to a Hedera network
            ContractFunctionResult getMessage = contractQuery.execute(hederaSdk.getMasterAccount());
            //Get the message
            String message = getMessage.getString(0);

            //Log the message
            System.out.println("The contract message: " + message);
            return message;
        }catch(PrecheckStatusException | TimeoutException | ReceiptStatusException ex){
            ex.printStackTrace();
            return null;
        }

    }

    public String callSmartContractBurnTokenFunction() {

        try {

            // Calls a function of the smart contract
            ContractCallQuery contractQuery = new ContractCallQuery()
                    //Set the gas for the query
                    .setGas(100000)
                    //Set the contract ID to return the request for
                    .setContractId(deployContract())
                    //Set the function of the contract to call
                    .setFunction("burn" )
                    //Set the query payment for the node returning the request
                    //This value must cover the cost of the request otherwise will fail
                    .setQueryPayment(new Hbar(2));

            //Submit to a Hedera network
            ContractFunctionResult getMessage = contractQuery.execute(hederaSdk.getMasterAccount());
            //Get the message
            String message = getMessage.getString(0);

            //Log the message
            System.out.println("The contract message: " + message);
            return message;
        }catch(PrecheckStatusException | TimeoutException | ReceiptStatusException ex){
            ex.printStackTrace();
            return null;
        }

    }
}
