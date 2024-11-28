package com.example.homiyummy.model.order;

import com.example.homiyummy.model.menu.MenuGetByNumEntity;
import com.example.homiyummy.model.user.UserReadResponse;
import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderWithRestaurantDataEntity {
    private String name_restaurant = "";
    private String num_order = "";
    private int date = 0;
    private ArrayList<MenuGetByNumEntity> menus = new ArrayList<>();
    private float total = 0;
    private String customerUID = "";

}
