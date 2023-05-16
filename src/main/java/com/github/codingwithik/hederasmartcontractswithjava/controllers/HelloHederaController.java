package com.github.codingwithik.hederasmartcontractswithjava.controllers;

import com.github.codingwithik.hederasmartcontractswithjava.services.HelloHederaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HelloHederaController {

    private final HelloHederaService helloHederaService;

    record MessageDto(String message){}

    @RequestMapping(value = "/message", method = RequestMethod.GET)
    public ResponseEntity<String> getHelloMessage(){
        return new ResponseEntity<>(helloHederaService.getMessage(), HttpStatus.OK);
    }

    @RequestMapping(value = "/message", method = RequestMethod.POST)
    public ResponseEntity<Void> setHelloMessage(@RequestBody MessageDto request){
        helloHederaService.setMessage(request.message);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
