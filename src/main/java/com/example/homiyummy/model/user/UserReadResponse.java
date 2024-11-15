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
public class UserReadResponse {
    private String email = "";
    private String name = "";
    private String surname = "";
    private String address = "";
    private String city = "";
    private String phone = "";
    private ArrayList<String> allergens = new ArrayList<>() ;
}