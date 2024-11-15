package com.example.homiyummy.model.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.ArrayList;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString

public class UserEntity {
    
    private String uid;
    private String name;
    private String surname;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String password;
    private ArrayList<String> allergens;



    public UserEntity(String uid, String name, String surname, String email, String address, String city, String phone, ArrayList<String> allergens){ // USO ESTE CONSTRUCTOR PARA COMUNCARME CON BBDD AL CREAR UN USUARIO
        this.uid = uid;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.address = address;
        this.city = city;
        this.phone = phone;
        this.allergens = allergens;
    }

    public UserEntity(String uid, String name,String surName, String phone, ArrayList<String> allergens) { // USADO PARA UPDATE USERS
        this.uid = uid;
        this.name = name;
        this.surname = surName;
        this.phone = phone;
        this.allergens = allergens;
    }
}