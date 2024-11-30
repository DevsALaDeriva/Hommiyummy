package com.example.homiyummy.model.order;

import com.example.homiyummy.model.menu.MenuSoldDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {
    private String num_order = "";
    private int date = 0;
    private String uidCustomer = "";
    private ArrayList<MenuSoldDTO> menus = new ArrayList<>();
    private float total = 0;
}
