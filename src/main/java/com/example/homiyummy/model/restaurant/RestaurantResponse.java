package com.example.homiyummy.model.restaurant;

import com.example.homiyummy.model.dish.DishResponse;
import com.example.homiyummy.repository.DishRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class RestaurantResponse {
    private String uid;
    private String email;
    private String name;
    private String description_mini;
    private String description;
    private String url;
    private String address;
    private String city;
    private String phone;
    private String schedule;
    private String image;
    private String food_type;
    private ArrayList<DishResponse> dishes;

    public RestaurantResponse(String email,
                              String name,
                              String description_mini,
                              String description,
                              String url,
                              String address,
                              String city,
                              String phone,
                              String schedule,
                              String image,
                              String food_type){
        this.email = email;
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
    }
}
