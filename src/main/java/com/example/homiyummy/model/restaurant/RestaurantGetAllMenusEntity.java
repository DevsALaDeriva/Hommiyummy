package com.example.homiyummy.model.restaurant;

import com.example.homiyummy.model.menu.MenuGetAllMenusEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RestaurantGetAllMenusEntity {
    private ArrayList<MenuGetAllMenusEntity> menus = new ArrayList<>();
}
