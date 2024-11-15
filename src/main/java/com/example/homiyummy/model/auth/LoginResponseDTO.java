package com.example.homiyummy.model.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.units.qual.N;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class LoginResponseDTO {
    private String uid;
    private String type;

}