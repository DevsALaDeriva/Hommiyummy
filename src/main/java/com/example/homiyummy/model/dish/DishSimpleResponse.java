package com.example.homiyummy.model.dish;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DishSimpleResponse {
    private int id;
    private String name;
    private String ingredients;
    private List<String> allergens;
    private String image;
}

