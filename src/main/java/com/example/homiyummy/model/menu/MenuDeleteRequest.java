package com.example.homiyummy.model.menu;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuDeleteRequest {
    private  String uid = "";
    private  int id = 0;
}
