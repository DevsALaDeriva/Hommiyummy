
package com.example.homiyummy.service;

import com.example.homiyummy.model.dish.DishDTO;
import com.example.homiyummy.model.dish.DishEntity;
import com.example.homiyummy.model.dish.DishResponse;
import com.example.homiyummy.model.menu.MenuDTO;
import com.example.homiyummy.model.menu.MenuEntity;
import com.example.homiyummy.model.menu.MenuResponse;
import com.example.homiyummy.model.menu.MenuResponseByPeriod;
import com.example.homiyummy.model.restaurant.RestaurantEntity;
import com.example.homiyummy.repository.DishRepository;
import com.example.homiyummy.repository.MenuRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class MenuService {
    
    @Autowired
    private final MenuRepository menuRepository;
    
    
    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public int findLastId(String uid){

        CompletableFuture<Integer> futureId = new CompletableFuture<Integer>();

        menuRepository.findId(uid, new MenuRepository.FindMenuIdCallback() {
            @Override
            public void onSuccess(Integer num) {
                futureId.complete(num);
            }

            @Override
            public void onFailure(DatabaseError error, Integer num) {
                futureId.complete(num); // SI DA ERROR ENVÍA UN 0, QUE ES LO QUE ESPERA EL FRONTEND
            }
        });
        try {
            return futureId.get();      // ENVIAMOS EL ID
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace(); // Log para identificar el error exacto
            return 0; // Devuelve 0 en caso de fallo para cumplir con las expectativas del frontend
        }
    }
    
    public MenuResponse create(MenuDTO menuDTO, int newMenuId) {
        
        CompletableFuture<MenuResponse> futureMenuResponse = new CompletableFuture<>();

        ArrayList<String> firstcourse = new ArrayList<>();
        ArrayList<String> secondcourse = new ArrayList<>();
        firstcourse.add(menuDTO.getFirstCourse().get(0));
        firstcourse.add(menuDTO.getFirstCourse().get(1));
        
        secondcourse.add(menuDTO.getSecondCourse().get(0));
        secondcourse.add(menuDTO.getSecondCourse().get(1));
        

        MenuEntity menuEntity = new MenuEntity();
        menuEntity.setUid(menuDTO.getUid());
        menuEntity.setId(newMenuId);
        menuEntity.setDate(menuDTO.getDate());
        menuEntity.setFirstCourse(firstcourse);
        menuEntity.setSecondCourse(secondcourse);
        menuEntity.setDessert(menuDTO.getDessert());
        menuEntity.setPriceWithDessert(menuDTO.getPriceWithDessert());
        menuEntity.setPriceNoDessert(menuDTO.getPriceNoDessert());

        CompletableFuture<MenuResponse> future = new CompletableFuture<>();
        
        
        menuRepository.save(menuEntity, new MenuRepository.SaveMenuCallback() {
            @Override
            public void onSuccess(MenuResponse menuResponse) {
                futureMenuResponse.complete(menuResponse);
            }

            @Override
            public void onFailure(Exception exception) {
                futureMenuResponse.completeExceptionally(exception);
            }
        });
        
        try {
            return futureMenuResponse.get();
        } catch (Exception e) {
            throw  new RuntimeException("Error al guardar el menu en Firebase");
        }
    }

    public Boolean updateMenu(MenuDTO menuDTO){
        //System.out.println("asfasdfasdfasdfasfd");

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        ArrayList<String> firstcourse = new ArrayList<>();
        ArrayList<String> secondcourse = new ArrayList<>();
        firstcourse.add(menuDTO.getFirstCourse().get(0));
        firstcourse.add(menuDTO.getFirstCourse().get(1));

        secondcourse.add(menuDTO.getSecondCourse().get(0));
        secondcourse.add(menuDTO.getSecondCourse().get(1));

        MenuEntity menuEntity = new MenuEntity();
        menuEntity.setUid(menuDTO.getUid());
        menuEntity.setId(menuDTO.getId());
        menuEntity.setDate(menuDTO.getDate());
        menuEntity.setFirstCourse(firstcourse);
        menuEntity.setSecondCourse(secondcourse);
        menuEntity.setDessert(menuDTO.getDessert());
        menuEntity.setPriceWithDessert(menuDTO.getPriceWithDessert());
        menuEntity.setPriceNoDessert(menuDTO.getPriceNoDessert());

        menuRepository.update(menuEntity, new MenuRepository.UpdateMenuCallback() {
            @Override
            public void onSuccess(Boolean result) {
                future.complete(result);
            }

            @Override
            public void onFailure(Exception exception) {
                future.completeExceptionally(exception);
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

    }

    public List<MenuResponseByPeriod> getMenusByDateRange(String uid, int startDate, int endDate) {
        CompletableFuture<List<MenuResponseByPeriod>> futureMenus = new CompletableFuture<>();

        menuRepository.findMenusByDateRange(uid, startDate, endDate, new MenuRepository.FindMenusCallback() {

            @Override
            public void onSuccess(List<MenuResponseByPeriod> menus) {
                futureMenus.complete(menus);
            }

            @Override
            public void onFailure(DatabaseError exception) {
                futureMenus.completeExceptionally(new RuntimeException("Error al obtener los menús: " + exception.getMessage()));
            }
        });

        try {
            return futureMenus.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error al procesar los menús", e);
        }
    }


    public CompletableFuture<Boolean> deleteMenu(String uid, int id){
        return menuRepository.delete(uid, id);
    }
    
    
    
    
}
