package com.example.homiyummy.model.order;

import com.example.homiyummy.model.menu.MenuGetByNumEntity;
import com.example.homiyummy.model.menu.MenuGetByNumResponse;
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
    private String uid = "";
    private int date = 0;
    private String uidCustomer = "";
    private ArrayList<MenuGetByNumResponse> menus = new ArrayList<>();
    private String status = "";
    private float total = 0;
}
