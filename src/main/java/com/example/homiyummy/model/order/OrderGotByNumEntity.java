package com.example.homiyummy.model.order;

import com.example.homiyummy.model.menu.MenuGetByNumEntity;
import com.example.homiyummy.model.menu.MenuGetByUrlEntity;
import com.example.homiyummy.model.reviews.ReviewsGetByNumOrderEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderGotByNumEntity {
//    private String uid = "";
//    private int date = 0;
//    private String uidCustomer = "";
//    private ArrayList<MenuGetByNumEntity> menus = new ArrayList<>();
//    private String status = "";
//    private float total = 0;

    private String uid = "";
    private int date = 0;
    private String uidCustomer = "";
    private ArrayList<MenuGetByNumEntity> menus = new ArrayList<>();
    private Object review = new ReviewsGetByNumOrderEntity(); // LO HACEMOS OBJECT PARA QUE SI NO PUEDE MANDAR EL OBJETO QUE QDUEREMOS MANDE UN MAP
    private String status = "";
    private float total = 0;
}
