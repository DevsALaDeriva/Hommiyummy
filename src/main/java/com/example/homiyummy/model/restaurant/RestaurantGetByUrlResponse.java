package com.example.homiyummy.model.restaurant;

import com.example.homiyummy.model.menu.MenuGetByUrlResponse;
import com.example.homiyummy.model.reviews.ReviewsResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RestaurantGetByUrlResponse {
    private String uid = "";
    private String name = "";
    private String food_type = "";
    private String address = "";
    private String image = "";
    private String phone = "";
    private String schedule = "";
    private Integer average_rate = 0;
    private String description = "";
    private String city = "";
    private ArrayList<ReviewsResponse> reviews = new ArrayList<>();
    private ArrayList<MenuGetByUrlResponse> menus = new ArrayList<>();
}
