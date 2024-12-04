package com.example.homiyummy.model.dish;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class DishGetByUrlEntity {
    private int id = 0;
    private String name = "";
    private String ingredients = "";
    private String allergens = "";
    private String image = "";
}