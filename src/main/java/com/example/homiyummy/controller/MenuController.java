package com.example.homiyummy.controller;

import com.example.homiyummy.model.menu.MenuDTO;
import com.example.homiyummy.model.menu.MenuResponse;
import com.example.homiyummy.service.AuthService;
import com.example.homiyummy.service.MenuService;
import com.example.homiyummy.service.RestaurantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/menu")
public class MenuController {
    
    private final MenuService menuService;
    //HOLA DESDE LA RAMA DEVELOP

    // ALFREDO ---------__> nuevo cambio
    
    //Adrian -------> nuevo cambio desde mi rama


    // Alfredo vamos pa llá

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }
    
    public ResponseEntity<String> registerMenu(@RequestBody MenuDTO menuDTO) {
        try {
            MenuResponse menuResponse = menuService.createMenu(menuDTO);
            
            return ResponseEntity.ok("{\"uid\": \"" + menuResponse.getUid() + "\"}");
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"uid\": 0 }");
        }
        
    }
}

