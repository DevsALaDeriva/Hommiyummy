package com.example.homiyummy.model.dish;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DishGetDayTaskResponse {
    private int id = 0;
    private String name = "";
    private String ingredients = "";
    private String allergerns = "";
    private String image = "";
}
