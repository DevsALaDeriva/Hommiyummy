package com.example.homiyummy.model.dish;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DishGetDayTaskEntity {
    private int id = 0;
    private String name = "";
    private String ingredients = "";
    private ArrayList<String> allergens = new ArrayList<>();
    private String image = "";

}
