package com.example.homiyummy.model.restaurant;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class RestaurantReadResponse {

    private String email = "";
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
    private RestaurantLocation location = new RestaurantLocation(0.0F, 0.0F);



}
