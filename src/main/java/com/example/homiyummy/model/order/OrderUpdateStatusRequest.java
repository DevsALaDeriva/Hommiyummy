package com.example.homiyummy.model.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdateStatusRequest {
    private String uid = "";
    private String num_order = "";
    private int id_menu = 0;
    private String status = "";
}
