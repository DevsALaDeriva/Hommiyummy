package com.example.homiyummy.service;

import com.example.homiyummy.model.menu.MenuDTO;
import com.example.homiyummy.model.menu.MenuEntity;
import com.example.homiyummy.model.menu.MenuResponse;
import com.example.homiyummy.model.restaurant.RestaurantEntity;
import com.example.homiyummy.repository.MenuRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class MenuService {
    
    @Autowired
    private final MenuRepository menuRepository;
    
    private final FirebaseDatabase firebaseDatabase;
    
    public MenuService(MenuRepository menuRepository, FirebaseDatabase firebaseDatabase) {
        this.menuRepository = menuRepository;
        this.firebaseDatabase = firebaseDatabase;
    }
    
    public MenuResponse createMenu(MenuDTO menuDTO) {
        MenuEntity menuEntity = new MenuEntity();
        
        menuEntity.setUid(menuDTO.getUid());
        menuEntity.setDate(menuDTO.getDate());
        menuEntity.setFirstCourse(menuDTO.getFirstCourse());
        menuEntity.setSecondCourse(menuDTO.getSecondCourse());
        menuEntity.setDessert(menuDTO.getDessert());
        menuEntity.setPriceWithDessert(menuDTO.getPriceWithDessert());
        menuEntity.setPriceNoDessert(menuDTO.getPriceNoDessert());

        CompletableFuture<MenuResponse> future = new CompletableFuture<>();
        
        menuRepository.saveMenu(menuEntity, new MenuRepository.getSaveMenuCallback() {
            @Override
            public void onMenuGot(MenuResponse menuResponse) {
                future.complete(menuResponse);
            }

            @Override
            public void onFailure(Exception exception) {
                future.completeExceptionally(exception);
            }
        });
        
        try {
            return future.get();
        } catch (Exception e) {
            throw  new RuntimeException("Error al guardar el menu en Firebase");
        }
    }
    
}
