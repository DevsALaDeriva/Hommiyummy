package com.example.homiyummy.model.restaurant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeaturedRestaurantResponse {
    private String uid = "";
    private String name = "";
    private String description = "";
    private String url = "";
    private String image = "";
    private String food_type = "";
}
