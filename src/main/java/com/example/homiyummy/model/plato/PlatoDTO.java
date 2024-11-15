package com.example.homiyummy.model.plato;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class PlatoDTO {
    private String uid = "";
    private int id = 0;
    private String name = "";
    private String ingredients = "";
    private ArrayList<String> allergens = new ArrayList<>();;
    private String type = "";

}
