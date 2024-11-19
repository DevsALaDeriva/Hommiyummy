package com.example.homiyummy.model.restaurant;

import com.example.homiyummy.model.dish.DishEntity;
import com.example.homiyummy.model.dish.DishResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class RestaurantEntity {
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
    private ArrayList<DishEntity> dishes;
}