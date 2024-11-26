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
public class OrderDTO {
    String uid = "";
    String numOrder = "";
    int date = 0;
    String customerUid = "";
    ArrayList<MenuSoldDTO> menus = new ArrayList<>();
    float total = 0;
}
