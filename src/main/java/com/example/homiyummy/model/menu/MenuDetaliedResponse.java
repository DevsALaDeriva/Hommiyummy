package com.example.homiyummy.model.menu;



import com.example.homiyummy.model.dish.DishEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuDetaliedResponse {
    private int id;
    private int date;
    private List<DishEntity> first_course; // Lista con los detalles completos de los platos principales
    private List<DishEntity> second_course; // Lista con los detalles completos de los platos secundarios
    private DishEntity dessert; // Postre con detalles completos
    private float priceWithDessert;
    private float priceNoDessert;
}

