package com.example.homiyummy.model.restaurant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

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
    private String food_type = "";
    private Integer rate = 0;
    private Float average_price = 0.0F;
    private RestaurantLocation location = new RestaurantLocation(0.0F,0.0F);


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
                          String food_type,
                          Integer rate,
                          Float average_price,
                          RestaurantLocation location
                          ){
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
        this.food_type = food_type;
        this.rate = rate;
        this.average_price = average_price;
        this.location = location;
    }

}



