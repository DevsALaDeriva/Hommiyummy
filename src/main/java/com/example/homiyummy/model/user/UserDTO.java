package com.example.homiyummy.model.user;

import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter

public class UserDTO implements Serializable {

    private String uid = "";
    private String name = "";
    private String surname = "";
    private String email = "";
    private String address = "";
    private String city = "";
    private String phone = "";
    private String password = "";
    private ArrayList<String> allergens = new ArrayList<>();

    public UserDTO(String uid, String name,String surName, String phone, ArrayList<String> allergens ) { // USADO PARA UPDATE USERS
        this.uid = uid;
        this.name = name;
        this.surname = surName;
        this.phone = phone;
        this.allergens = allergens;
    }

    public UserDTO(String uid){  // LO USO EN AuthService     getUserDtoFromToken
        this.uid = uid;
    }


}