package com.github.codingwithik.hederasmartcontractswithjava.dto;

//(string calldata _regulator, string calldata _role, address _userOwner, string calldata _country)
public record UserDto(String regulatorName, String role, String country) {
}
