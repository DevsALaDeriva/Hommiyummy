package com.example.homiyummy.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * ESTE SERVICIO SE CREA PORQUE NECESITAMOS UN SERVICIO QUE SE COMUNIQUE (VIA REPOSITORIO) CON REALTIME.
 * NO PODEMOS USAR EL UserService PORQUE ES EXCLUSIVO DE USER
 * NO PODEMOS USAR RestauranteService PORQUE ES EXCLUSIVO DE RESTAURANT
 * CREAMOS UN SERVICE NUEVO QUE DERIVE LA CONSULTA A UserService Y RestauranteService
 */
@Service
public class UserTypeService {

    private final UserService userService;
    private final RestaurantService restaurantService;

    public UserTypeService(UserService userService, RestaurantService restaurantService) {
        this.userService = userService;
        this.restaurantService = restaurantService;
    }

    public CompletableFuture<String> getUserTypeByUid(String uid) {
        CompletableFuture<String> userTypeFuture = new CompletableFuture<>();

        userService.existsByUid(uid).thenAccept(userExists -> {
            if (userExists) {
                userTypeFuture.complete("customer");
            } else {
                restaurantService.existsByUid(uid).thenAccept(restaurantExists -> {
                    if (restaurantExists) {
                        userTypeFuture.complete("restaurant");
                    } else {
                        userTypeFuture.completeExceptionally(new RuntimeException("Tipo de usuario no encontrado para UID: " + uid));
                    }
                });
            }
        });

        return userTypeFuture;
    }

}