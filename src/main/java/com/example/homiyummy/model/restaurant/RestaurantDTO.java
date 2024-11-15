package com.example.homiyummy.model.restaurant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class RestaurantDTO {
    private String uid = "";
    private String email = "";
    private String password = "";
    private String name = "";
    private String description_mini = "";
    private String description = "";
    private String url = "";
    private String address = "";
    private String city = "";
    private String phone = "";
    private String schedule = "";
    private String image = "";
    private String foodType = "";



    // USADO PARA EL endpoint DE UPDATE
    public RestaurantDTO (String uid,
                          String name,
                          String description_mini,
                          String description,
                          String url,
                          String address,
                          String city,
                          String phone,
                          String schedule,
                          String image,
                          String foodtype){
        this.uid = uid;
        this.name = name;
        this.description_mini = description_mini;
        this.description = description;
        this.url = url;
        this.address = address;
        this.city = city;
        this.phone = phone;
        this.schedule = schedule;
        this.image = image;
        this.foodType = foodtype;
    }

}



