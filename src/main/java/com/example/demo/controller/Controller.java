package com.example.demo.controller;


import org.springframework.http.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class Controller {



    @GetMapping("/")
    public String hello(@RequestParam(value = "name", defaultValue = "Worlds") String name){

        return String.format("Hello %s!", name);
    }


    @GetMapping("/home")
    public ResponseEntity<String> homePageRoute()  {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "text/html; charset=UTF-8");

        return ResponseEntity.status(HttpStatus.OK).headers(headers).body("Welcome to the home Page.");

    }


    @GetMapping("/user/home")
    public ResponseEntity<String> userProfileRoute()  {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "text/html; charset=UTF-8");

        return ResponseEntity.status(HttpStatus.OK).headers(headers).body("User home Page");

    }


    @GetMapping("/admin/home")
    public ResponseEntity<String> adminProfileRoute()  {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "text/html; charset=UTF-8");

        return ResponseEntity.status(HttpStatus.OK).headers(headers).body("Admin home Page");


    }




}
