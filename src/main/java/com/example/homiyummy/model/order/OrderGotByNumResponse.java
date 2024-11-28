package com.example.homiyummy.model.order;

import com.example.homiyummy.model.menu.MenuGetByNumEntity;
import com.example.homiyummy.model.user.UserReadResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderGotByNumResponse {
    private String name_restaurant = "";
    private String num_order = "";
    private int date = 0;
    private UserReadResponse customer = new UserReadResponse();
    private ArrayList<MenuGetByNumEntity> menus = new ArrayList<>();
    private float total = 0;
}
