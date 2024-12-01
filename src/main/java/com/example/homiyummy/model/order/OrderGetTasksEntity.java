package com.example.homiyummy.model.order;

import com.example.homiyummy.model.menu.MenuInGetTaskEntity;
import com.example.homiyummy.model.menu.MenuInGetTasksResponse;
import com.example.homiyummy.model.user.UserInGetTaskEntity;
import com.example.homiyummy.model.user.UserInGetTasksResponse;
import lombok.*;

@AllArgsConstructor // CONSTRUCTOR PRINCIPAL USADO PARA CREAR UN USUARIO
@NoArgsConstructor
@ToString
@Getter
@Setter

public class OrderGetTasksEntity {
    private String num_order = "";
    MenuInGetTaskEntity menu = new MenuInGetTaskEntity();
    UserInGetTaskEntity customer = new UserInGetTaskEntity();
}
