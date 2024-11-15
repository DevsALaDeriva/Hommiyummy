package com.example.homiyummy.model.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
/**
 * CLASE CREADA PQ NECESITABA RECIBIR UN EMAIL DENTRO DE UN JSON Y DABA ERROR SI NO ERA ASÍ.
 * 	EmailRequest: Esta clase permite recibir el email en el formato JSON adecuado.
 * 	Uso de emailRequest.getEmail(): En el controlador, ahora extraemos el email desde emailRequest, asegurando que el formato de datos sea el correcto para la búsqueda en Firebase Authentication.
 */
public class EmailRequest {
    private String email;
}