package com.example.homiyummy.model.restaurant;

import com.example.homiyummy.model.menu.MenuGetByNumEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RestaurantGetByOrderNumberEntity {
    private String name_restaurant = "";
    private Integer date = 0;
    private ArrayList<MenuGetByNumEntity> menus = new ArrayList<>();
    private float total = 0;
}
