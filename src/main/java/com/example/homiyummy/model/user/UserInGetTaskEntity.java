package com.example.homiyummy.model.user;

import lombok.*;
import java.util.ArrayList;

@AllArgsConstructor // CONSTRUCTOR PRINCIPAL USADO PARA CREAR UN USUARIO
@NoArgsConstructor
@ToString
@Getter
@Setter

public class UserInGetTaskEntity {
    private String name = "";
    private String surname = "";
    private String phone = "";
    private String email = "";
    private ArrayList<String> allergens = new ArrayList<>();
}
