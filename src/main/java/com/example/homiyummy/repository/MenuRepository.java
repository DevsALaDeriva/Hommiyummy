package com.example.homiyummy.repository;

import com.example.homiyummy.model.dish.DishEntity;
import com.example.homiyummy.model.dish.DishResponse;
import com.example.homiyummy.model.dish.DishUpdateEntity;
import com.example.homiyummy.model.menu.*;
import com.example.homiyummy.service.RestaurantService;
import com.google.firebase.database.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Repository
public class MenuRepository {

    private final  DatabaseReference databaseReference;
    
    private final RestaurantRepository restaurantRepository;

    private final RestaurantService restaurantService;

    public MenuRepository(DatabaseReference databaseReference, RestaurantRepository restaurantRepository, RestaurantService restaurantService) {

        this.databaseReference = databaseReference;
        this.restaurantRepository = restaurantRepository;
        this.restaurantService = restaurantService;
    }
    public void findId(String uid, MenuRepository.FindMenuIdCallback callback){

        DatabaseReference restaurantRef = databaseReference.child("restaurants").child(uid);
        DatabaseReference menuRef = restaurantRef.child("/menus");
        DatabaseReference idRef = menuRef.child("/counter");

        idRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Integer id = dataSnapshot.getValue(Integer.class);
                    if (id != null) {
                        callback.onSuccess(id);
                    } else {
                        callback.onSuccess(0); // En caso de datos no válidos
                    }
                }
                else{
                    callback.onSuccess(0);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFailure(databaseError, 0);
                //System.err.println("Error al obtener el ID: " + databaseError.getMessage());
            }
        });
    }
    
    public void save(MenuEntity menuEntity, SaveMenuCallback callback) {
        
        String uid = menuEntity.getUid();
        DatabaseReference restaurantRef = databaseReference.child("restaurants").child(uid);
        DatabaseReference menusRef = restaurantRef.child("menus");
        DatabaseReference counterRef = menusRef.child("counter");
        DatabaseReference itemRef = menusRef.child("items");
        
        DatabaseReference menuRef = itemRef.child(String.valueOf(menuEntity.getId()));

        MenuSaveEntity menuSaveEntity = new MenuSaveEntity(
                menuEntity.getId(),
                menuEntity.getDate(),
                menuEntity.getFirstCourse(),
                menuEntity.getSecondCourse(),
                menuEntity.getDessert(),
                menuEntity.getPriceWithDessert(),
                menuEntity.getPriceNoDessert());

        counterRef.setValue(menuEntity.getId(), (databaseCounterError, databaseCounterReference) -> {
            if(databaseCounterError != null){
                callback.onFailure(new Exception("Error al actualizar el contador: " + databaseCounterError.getMessage()));
                return;
            }

            menuRef.setValue(menuSaveEntity, ((databaseMenuError, databaseMenuReference) -> {
                if(databaseMenuError != null) {
                    callback.onFailure(new Exception("Error al guardar el menu: " + databaseMenuError.getMessage()));
                    return;
                }

                menuRef.addListenerForSingleValueEvent(new ValueEventListener() {                                       // FOTO DE ESE PLATO
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            MenuEntity savedMenuEntity = dataSnapshot.getValue(MenuEntity.class);
                            if(savedMenuEntity != null) {
                                MenuResponse menuResponse = new MenuResponse(
                                        savedMenuEntity.getUid(),
                                        savedMenuEntity.getId(),
                                        savedMenuEntity.getDate(),
                                        savedMenuEntity.getFirstCourse(),
                                        savedMenuEntity.getSecondCourse(),
                                        savedMenuEntity.getDessert(),
                                        savedMenuEntity.getPriceWithDessert(),
                                        savedMenuEntity.getPriceNoDessert());
                                        
                                callback.onSuccess(menuResponse);
                            }
                            else {
                                MenuResponse menuResponse = new MenuResponse();
                                menuResponse.setId(0);
                                callback.onSuccess(menuResponse); // DEVUELVO UN DISRESPONSE VACIO, CON EL ID:0
                            }
                        }
                        else {
                            callback.onFailure(new Exception("No se encontró el menu guardado en la base de datos"));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        callback.onFailure(new Exception("Error al leer el menu guardado: " + databaseError.getMessage()));
                    }
                });
            }));
        });
    }

    public void update(MenuEntity menuEntity, UpdateMenuCallback callback) {
        // Nodo del menú que se va a actualizar
        DatabaseReference menuRef = databaseReference.child("restaurants")
                .child(menuEntity.getUid())
                .child("menus/items")
                .child(String.valueOf(menuEntity.getId()));

        // Verificar si el menú existe
        menuRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Leer datos actuales de Firebase si es necesario
                    ArrayList<Integer> currentFirstCourse = (ArrayList<Integer>) dataSnapshot.child("firstCourse").getValue();
                    ArrayList<Integer> currentSecondCourse = (ArrayList<Integer>) dataSnapshot.child("secondCourse").getValue();

                    // Crear una nueva entidad para actualizar el menú
                    MenuSaveEntity menuEntityToBeSaved = new MenuSaveEntity();
                    menuEntityToBeSaved.setId(menuEntity.getId());
                    menuEntityToBeSaved.setDate(menuEntity.getDate());
                    menuEntityToBeSaved.setFirstCourse(menuEntity.getFirstCourse() != null ? menuEntity.getFirstCourse() : currentFirstCourse);
                    menuEntityToBeSaved.setSecondCourse(menuEntity.getSecondCourse() != null ? menuEntity.getSecondCourse() : currentSecondCourse);
                    menuEntityToBeSaved.setDessert(menuEntity.getDessert());
                    menuEntityToBeSaved.setPriceWithDessert(menuEntity.getPriceWithDessert());
                    menuEntityToBeSaved.setPriceNoDessert(menuEntity.getPriceNoDessert());

                    // Guardar los cambios en Firebase
                    menuRef.setValue(menuEntityToBeSaved, (databaseError, databaseReference1) -> {
                        if (databaseError != null) {
                            callback.onFailure(new Exception("Error al guardar el menú: " + databaseError.getMessage()));
                            return;
                        }

                        // Verificar que los datos se guardaron correctamente
                        menuRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    callback.onSuccess(true);
                                } else {
                                    callback.onSuccess(false);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                callback.onFailure(new Exception("Error al leer el menú guardado: " + databaseError.getMessage()));
                            }
                        });
                    });
                } else {
                    // Si no existe el menú
                    callback.onFailure(new Exception("El menú no existe en la base de datos."));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFailure(new Exception("Error al verificar la existencia del menú: " + databaseError.getMessage()));
            }
        });
    }

    public CompletableFuture<Boolean> delete(String uid, int menuId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        DatabaseReference dishRef = databaseReference.child("restaurants")
                .child(uid)
                .child("menus/items")
                .child(String.valueOf(menuId));

        dishRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    dishRef.removeValue((databaseError, databaseReference1) -> {
                        if (databaseError != null) {
                            future.completeExceptionally(databaseError.toException());
                        } else {
                            future.complete(true);
                        }
                    });
                } else {
                    future.complete(false); // Objeto no encontrado
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }

    public void findMenusByDateRange(String uid, int startDate, int endDate, FindMenusCallback callback) {
        DatabaseReference restaurantRef = databaseReference.child("restaurants").child(uid);
        DatabaseReference menuItemsRef = restaurantRef.child("menus/items");
     
        menuItemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<MenuResponseByPeriod> menus = new ArrayList<>();
                    for (DataSnapshot menuSnapshot : dataSnapshot.getChildren()) {
                        MenuSaveEntity menu = menuSnapshot.getValue(MenuSaveEntity.class);
                        if (menu != null && menu.getDate() >= startDate && menu.getDate() <= endDate) {
                            menus.add(new MenuResponseByPeriod(
                                    menu.getId(),
                                    menu.getDate(),
                                    menu.getFirstCourse(),
                                    menu.getSecondCourse(),
                                    menu.getDessert(),
                                    menu.getPriceWithDessert(),
                                    menu.getPriceNoDessert()
                            ));
                        }
                    }
                    callback.onSuccess(menus);
                } else {
                    callback.onSuccess(new ArrayList<>());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFailure(databaseError);
            }
        });
    }

    public void findMenuById(String uid, int menuId, FindMenuByIdCallback callback) {
        DatabaseReference menuRef = databaseReference.child("restaurants")
                .child(uid)
                .child("menus/items")
                .child(String.valueOf(menuId));

        menuRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    MenuEntityById menu = dataSnapshot.getValue(MenuEntityById.class);
                    if (menu != null) {
                        callback.onSuccess(new MenuByIdResponse(
                                menu.getId(),
                                menu.getDate(),
                                menu.getFirstCourse(),
                                menu.getSecondCourse(),
                                menu.getDessert(),
                                menu.getPriceWithDessert(),
                                menu.getPriceNoDessert()
                        ));
                    } else {
                        callback.onFailure(new Exception("Menu no encontrado o datos inválidos"));
                    }
                } else {
                    callback.onFailure(new Exception("No existe un menú con ese ID"));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFailure(new Exception("Error al acceder a la base de datos: " + databaseError.getMessage()));
            }
        });
    }

    public interface FindMenuByIdCallback {
        void onSuccess(MenuByIdResponse menu);
        void onFailure(Exception exception);
    }


    public interface FindMenuIdCallback{
        void onSuccess(Integer id);
        void onFailure(DatabaseError exception, Integer id);
    }

    public interface getSaveMenuCallback {
        void onMenuGot(MenuResponse menuResponse);
        void onFailure(Exception exception);
    }

    public interface SaveMenuCallback{
        void onSuccess(MenuResponse menuResponse);
        void onFailure(Exception exception);
    }

    public interface UpdateMenuCallback{
        void onSuccess(Boolean result);
        void onFailure(Exception exception);
    }

    public interface FindMenusCallback {
        void onSuccess(List<MenuResponseByPeriod> menus);
        void onFailure(DatabaseError exception);
    }
}
