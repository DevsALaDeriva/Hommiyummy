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

public class DishInOrderEntity {
    private String name = "";
    private String ingredients = "";
    private String allergens = "";
    private String image = "";
}
