package com.example.homiyummy.model.menu;

import com.example.homiyummy.model.dish.DishGetByEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuGetAllMenusEntity {

    private int id = 0;
    private int date = 0;
    private ArrayList<DishGetByEntity> first_course = new ArrayList<>();
    private ArrayList<DishGetByEntity> second_course = new ArrayList<>();
    private DishGetByEntity dessert = new DishGetByEntity();
    private float priceWithDessert = 0;
    private float priceNoDessert = 0;
}
