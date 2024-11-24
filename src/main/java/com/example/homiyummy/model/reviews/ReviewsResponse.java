package com.example.homiyummy.model.reviews;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewsResponse {
    private String name = "";
    private String review = "";
    private int rate = 0;
}
