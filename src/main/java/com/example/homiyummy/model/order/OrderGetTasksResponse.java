package com.example.homiyummy.model.order;

import com.example.homiyummy.model.menu.MenuInGetTasksResponse;
import com.example.homiyummy.model.user.UserInGetTasksResponse;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter

public class OrderGetTasksResponse {
    private String num_order = "";
    MenuInGetTasksResponse menu = new MenuInGetTasksResponse();
    UserInGetTasksResponse customer = new UserInGetTasksResponse();
}
