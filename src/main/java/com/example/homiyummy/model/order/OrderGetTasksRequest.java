package com.example.homiyummy.model.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderGetTasksRequest {
    private String uid = "";
    private int start_date = 0;
    private int end_date = 0;
}
