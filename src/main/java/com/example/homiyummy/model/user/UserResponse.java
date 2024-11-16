package com.example.homiyummy.model.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String uid;


    private String name;
    private String surname;
    private String email;
    private String address;
    private String city;
    private String phone;
    private ArrayList<String> allergens ;


    public UserResponse(String uid){
        this.uid = uid;
    }

}