package com.example.homiyummy.model.restaurant;

import com.example.homiyummy.model.menu.MenuGetAllMenusEntity;
import com.example.homiyummy.model.menu.MenuGetAllMenusResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RestaurantGetAllMenusResponse {
    private ArrayList<MenuGetAllMenusResponse> menus = new ArrayList<>();

}
