package com.example.homiyummy.controller;

import com.example.homiyummy.model.menu.*;
import com.example.homiyummy.service.MenuService;
import com.example.homiyummy.service.RestaurantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/menu")
public class MenuController {
    
    private final MenuService menuService;
    private final RestaurantService restaurantService;
    //HOLA DESDE LA RAMA DEVELOP

    // ALFREDO ---------__> nuevo cambio
    
    //Adrian -------> nuevo cambio desde mi rama


    public MenuController(MenuService menuService, RestaurantService restaurantService) {
        this.menuService = menuService;
        this.restaurantService = restaurantService;
    }
    
    @PostMapping("/create")
    public ResponseEntity<String> registerMenu(@RequestBody MenuDTO menuDTO) {
        
        String uid = menuDTO.getUid();

        if(!uid.isEmpty()) {

            CompletableFuture<Boolean> future = new CompletableFuture<>();
            
            restaurantService.existsByUid(uid).thenAccept(exist ->
                    future.complete(exist));
            Boolean restaurantExists;
            try {
                restaurantExists = future.get(); // EXISTE O NO EXISTE

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return new ResponseEntity<>("{\"id\": 0}", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if(restaurantExists){ // SI EXISTE SEGUIMOS

                // 2º AVERIGÜAMOS EL ID DEL ÚLTIMO PLATO GUARDADO
                int lastIdSaved = menuService.findLastId(uid);
                int newDishId = lastIdSaved + 1;
                //System.out.println("NEW DISH ID -1--------------> " + newDishId);

                // 3º TRAMITAMOS EL GUARDADO DE DATOS
                try{
                    MenuResponse menuResponse = menuService.create(menuDTO, newDishId);
                    //System.out.println("NEW DISH ID -2--------------> " + newDishId);
                    return new ResponseEntity<>("{\"id\": \"" + menuResponse.getId() + "\"}", HttpStatus.OK);
                }
                catch (RuntimeException e) {
                    e.printStackTrace(); // Log para identificar el error exacto
                    return new ResponseEntity<>("{\"id\": 0}", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            else{
                return new ResponseEntity<>("{\"id\": 0}", HttpStatus.NOT_FOUND);
            }
            
        } else {
            return new ResponseEntity<>("{\"id\": 0}", HttpStatus.NOT_FOUND);
        }
        
    }
    @PostMapping("/update")
    public ResponseEntity<String> update(@RequestBody MenuDTO menuDTO) {
        
        String uid = menuDTO.getUid();

        if(!uid.isEmpty()) {

            CompletableFuture<Boolean> future = new CompletableFuture<>();
            
            restaurantService.existsByUid(uid).thenAccept(exists ->        
                    future.complete(exists));

            Boolean restaurantExists;
            try {
                restaurantExists = future.get(); // EXISTE O NO EXISTE
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            

            if (restaurantExists) {
                Boolean result = menuService.updateMenu(menuDTO);
                return new ResponseEntity<>("{\"change\": \"" + result + "\"}", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("{\"change\": \"" + false + "\"}", HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity<>("{\"change\": \"" + false + "\"}", HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping("/delete")
    public CompletableFuture<ResponseEntity<MenuDeleteResponse>> delete(@RequestBody MenuDeleteRequest menuDeleteRequest) {
        String uid = menuDeleteRequest.getUid();
        int id = menuDeleteRequest.getId();

        if (!uid.isEmpty()) {
            
            return menuService.deleteMenu(uid, id).thenApply( success -> 
                    new ResponseEntity<>(new MenuDeleteResponse(success), HttpStatus.OK));
        }
        else {
            MenuDeleteResponse menuDeleteResponse = new MenuDeleteResponse();
            menuDeleteResponse.setDelete(false);
            return CompletableFuture.completedFuture(
                    new ResponseEntity<>(menuDeleteResponse, HttpStatus.NOT_FOUND));
        }
        
    }

    @PostMapping("/getById")
    public ResponseEntity<MenuByIdResponse> getById(@RequestBody MenuByIdRequest menuByIdRequest) {
        String uid = menuByIdRequest.getUid();
        int id = menuByIdRequest.getId();

        if (uid.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        try{
            MenuByIdResponse menu = menuService.getMenuById(uid, id);
            return new ResponseEntity<>(menu, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}

