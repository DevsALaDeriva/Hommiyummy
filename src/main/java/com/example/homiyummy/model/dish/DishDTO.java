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

public class DishDTO {
    private String uid = "";
    private int id = 0;
    private String name = "";
    private String ingredients = "";
    private ArrayList<String> allergens = new ArrayList<>();;
    private String type = "";

//    public PlatoDTO(String uid, int id, String name, String ingredients, ArrayList<String> allergens, String type){
//        this.uid = uid;
//        this.id = id;
//        this.name = name;
//        this.ingredients = ingredients;
//        this.allergens = allergens;
//        this.type = type;
//    }

}
