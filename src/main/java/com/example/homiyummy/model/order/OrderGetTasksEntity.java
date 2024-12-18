package com.example.homiyummy.model.order;

import com.example.homiyummy.model.menu.MenuInGetTaskEntity;
import com.example.homiyummy.model.user.UserInGetTaskEntity;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter

public class OrderGetTasksEntity {
    private String num_order = "";
    MenuInGetTaskEntity menu = new MenuInGetTaskEntity();
    UserInGetTaskEntity customer = new UserInGetTaskEntity();
}
