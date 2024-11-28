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


public class DishEntity {
    private String uid = "";
    private Integer id = 0;
    private String name = "";
    private String ingredients = "";
    private ArrayList<String> allergens = new ArrayList<>();;
    private String image = "";
    private String type = "";

    public DishEntity(Integer id, String name, String ingredients, ArrayList<String> allergens, String image, String type){
        this.id = id;
        this.name = name;
        this.ingredients = ingredients;
        this.allergens = allergens;
        this.image = image;
        this.type = type;
    }

//    public DishEntity(String name, String ingredients, ArrayList<String> allergens, String image){
//        this.name = name;
//        this.ingredients = ingredients;
//        this.allergens = allergens;
//        this.image = image;
//    }
}
