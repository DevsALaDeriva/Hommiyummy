package com.example.homiyummy.model.user;

import lombok.*;

import java.util.ArrayList;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserFindEntity {
    private String email = "";
    private String name = "";
    private String surname = "";
    private String address = "";
    private String city = "";
    private String phone = "";
    private ArrayList<String> allergens = new ArrayList<>() ;
}
