package com.example.homiyummy.model.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChangePassRequest { // CLASE CREADA PARA MANDAR UN OBJETO DE ESTE TIPO AL CONSTRUCTOR DEL CONTROLLER CON endpoint "/changePassword"
    private String uid = "";
    private String password = "";

}
