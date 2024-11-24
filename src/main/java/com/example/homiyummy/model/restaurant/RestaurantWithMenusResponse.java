package com.example.homiyummy.model.restaurant;

import com.example.homiyummy.model.menu.MenuReadResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class RestaurantWithMenusResponse {
    private String name = "";
    private String description_mini = "";
    private String url = "";
    private String address = "";
    private String phone = "";
    private String schedule = "";
    private String food_type = "";
    private String image = "";
    private Integer rate = 0;
    private Float average_price = 0.0F;
    private RestaurantLocation location = new RestaurantLocation(0.0F, 0.0F);
    private ArrayList<MenuReadResponse> menus = new ArrayList<>();
}
