package com.github.codingwithik.hederasmartcontractswithjava;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HederaSmartContractsWithJavaApplication implements CommandLineRunner {

	@Value("${MasterAccountID}")
	private String masterAccountId;

	@Value("${MasterPrivateKey}")
	private String masterPrivateKey;

	@Value("${hedera.network}")
	private String hederaNetwork;

	public static void main(String[] args) {
		SpringApplication.run(HederaSmartContractsWithJavaApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println(masterAccountId);
		System.out.println(masterPrivateKey);
		System.out.println(hederaNetwork);
	}
}
