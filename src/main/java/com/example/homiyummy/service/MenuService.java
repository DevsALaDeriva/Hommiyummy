
package com.example.homiyummy.service;

import com.example.homiyummy.model.dish.MenuSimpleResponse;
import com.example.homiyummy.model.menu.*;
import com.example.homiyummy.repository.MenuRepository;
import com.google.firebase.database.DatabaseError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        ArrayList<Integer> firstcourse = new ArrayList<>();
        ArrayList<Integer> secondcourse = new ArrayList<>();
        firstcourse.add(menuDTO.getFirst_course().get(0));
        firstcourse.add(menuDTO.getFirst_course().get(1));
        
        secondcourse.add(menuDTO.getSecond_course().get(0));
        secondcourse.add(menuDTO.getSecond_course().get(1));
        

        MenuEntity menuEntity = new MenuEntity();
        menuEntity.setUid(menuDTO.getUid());
        menuEntity.setId(newMenuId);
        menuEntity.setDate(menuDTO.getDate());
        menuEntity.setFirst_course(firstcourse);
        menuEntity.setSecond_course(secondcourse);
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

        ArrayList<Integer> firstcourse = new ArrayList<>();
        ArrayList<Integer> secondcourse = new ArrayList<>();
        firstcourse.add(menuDTO.getFirst_course().get(0));
        firstcourse.add(menuDTO.getFirst_course().get(1));

        secondcourse.add(menuDTO.getSecond_course().get(0));
        secondcourse.add(menuDTO.getSecond_course().get(1));

        MenuEntity menuEntity = new MenuEntity();
        menuEntity.setUid(menuDTO.getUid());
        menuEntity.setId(menuDTO.getId());
        menuEntity.setDate(menuDTO.getDate());
        menuEntity.setFirst_course(firstcourse);
        menuEntity.setSecond_course(secondcourse);
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

    public List<MenuSimpleResponse> getSimpleMenusByDateRange(String uid, int startDate, int endDate) {

        CompletableFuture<List<MenuSimpleResponse>> futureMenus = new CompletableFuture<>();

        menuRepository.findMenusWithSimpleDetails(uid, startDate, endDate, new MenuRepository.FindSimpleMenusCallback() {
            @Override
            public void onSuccess(List<MenuSimpleResponse> menus) {
                futureMenus.complete(menus); // Completar el futuro con la lista de menús
            }

            @Override
            public void onFailure(DatabaseError exception) {
                futureMenus.completeExceptionally(new RuntimeException("Error al obtener los menús simples: " + exception.getMessage()));
            }
        });

        try {
            return futureMenus.get(); // Bloquea hasta que el futuro se complete
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error al procesar los menús simples", e);
        }
    }




    public CompletableFuture<Boolean> deleteMenu(String uid, int id){
        return menuRepository.delete(uid, id);
    }

    public MenuByIdResponse getMenuById(String uid, int menuId) {
        CompletableFuture<MenuByIdResponse> futureMenu = new CompletableFuture<>();

        menuRepository.findMenuById(uid, menuId, new MenuRepository.FindMenuByIdCallback() {

            @Override
            public void onSuccess(MenuByIdResponse menu) {
                futureMenu.complete(menu);
            }

            @Override
            public void onFailure(Exception exception) {
                futureMenu.completeExceptionally(exception);
            }
        });

        try {
            return futureMenu.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error al obtener el menú por ID: " + e.getMessage(), e);
        }
    }



}
