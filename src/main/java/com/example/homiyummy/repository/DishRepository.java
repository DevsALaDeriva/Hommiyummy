package com.example.homiyummy.repository;

import com.example.homiyummy.model.dish.*;
import com.example.homiyummy.service.RestaurantService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Repository
public class DishRepository {

    private final  DatabaseReference databaseReference;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantService restaurantService;
    public DishRepository(DatabaseReference databaseReference, RestaurantRepository restaurantRepository, RestaurantService restaurantService){
        this.databaseReference = databaseReference;
        this.restaurantRepository = restaurantRepository;
        this.restaurantService = restaurantService;
    }


    /**
     *  OBTIENE ÚLTIMO ID GRABADO PARA UN PLATO
     * @param uid IDENTIFICADOR ÚNICO DEL RESTAURANTE
     * @param callback DEVUELVE EL ID DEL ÚLTIMO PLATO GUARDADO O 0 SI NO LO HAY
     */
    public void findId(String uid, FindPlatoIdCallback callback){

        // GUARDAMOS LA REFERENCIA DEL RESTAURANTE
        DatabaseReference restaurantRef = databaseReference.child("restaurants").child(uid);
        // LA DEL NODO DE SUS PLATOS
        DatabaseReference dishRef = restaurantRef.child("/dishes");
        // EL CONTADOR QUE ALMACENA EL ID DEL ÚLTIMO PLATO GUARDADO
        DatabaseReference idRef = dishRef.child("/counter");

        // ACCEDEMOS AL NODO DEL CONTADOR
        idRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // SI EXISTE
                if(dataSnapshot.exists()){
                    // GUARDAMOS EL VALOR DE CONTADOR
                    Integer id = dataSnapshot.getValue(Integer.class);
                    // SI EXISTE
                    if (id != null) {
                        // LO DEVOLVEMOS AL SERVICE
                        callback.onSuccess(id);
                    } else {
                        // SI NO EXISTE MANDAMOS 0 COMO VALOR
                        callback.onSuccess(0); // En caso de datos no válidos
                    }
                }
                else{
                    // SI NO EXISTE MANDAMOS 0 COMO VALOR
                    callback.onSuccess(0);
                }
            }

            // MANDAMOS EXCEPCIÓN SI HAY UN ERROR
            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFailure(databaseError, 0);
            }
        });
    }


    /**
     *
     * @param dishEntity OJBETO EN QUE RECIBIMOS EL JSON ENTRANTE (FORMATO DishEntity)
     *                   LO CONVERTIMOS EN UN DishSaveEntity (ES LO MISMO, SIN EL UID DEL RESTAURATE)
     * @param callback DEVUELVE EN UN DishResponse EL PLATO RECIÉN GUARDADO Y RECIÉN OBTENIDO
     */
    public void save(DishEntity dishEntity, SavePlatoCallback callback){

        // GUARDAMOS EL UID DEL PLATO ENTRANTE
        String uid = dishEntity.getUid();
        // GUARDAMOS LA REFERENCIA DEL RESTAURANTE ENTRANTE
        DatabaseReference restaurantRef = databaseReference.child("restaurants").child(uid);
        // LA DE SUS PLATOS
        DatabaseReference allDishesRef = restaurantRef.child("dishes");
        // SU CONTADOR
        DatabaseReference counterRef = allDishesRef.child("counter");
        // EL NODO FINAL DONDE SE GUARDAN LOS PLATOS
        DatabaseReference itemsRef = allDishesRef.child("items");

        // CREAMOS EL NODO DEL PLATO QUE HAY QUE GUARDAR
        DatabaseReference dishRef = itemsRef.child(String.valueOf(dishEntity.getId()));

        // CREAMOS EL PLATO
        DishSaveEntity dishSaveEntity = new DishSaveEntity(
                dishEntity.getId(),
                dishEntity.getName(),
                dishEntity.getIngredients(),
                dishEntity.getAllergens(),
                dishEntity.getImage(),
                dishEntity.getType());

        // ASIGNAMOS AL CONTADOR SU NUEVO VALOR ( EL ID DEL NUEVO PLATO)
        counterRef.setValue(dishSaveEntity.getId(), (databaseCounterError, databaseCounterReference) -> {
            if(databaseCounterError != null){
                // SI SALE MAL MANDAMOS UNA EXCEPCIÓN Y SALIMOS
                callback.onFailure(new Exception("Error al actualizar el contador: " + databaseCounterError.getMessage()));
                return;
            }

            // ASIGNAMOS EL PLATO A SU NODO
            dishRef.setValue(dishSaveEntity, ((databaseDishError, databaseDishReference) -> {
                // SI SALE MAL
                if(databaseDishError != null) {
                    // MANDAMOS UNA EXCEPCIÓN Y SALIMOS
                    callback.onFailure(new Exception("Error al guardar el plato: " + databaseDishError.getMessage()));
                    return;
                }

                // SI SALE BIEN ACCEDEMOS AL PLATO
                dishRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // SI EXISTE
                        if(dataSnapshot.exists()){
                            // GUARDAMOS EL PLATO EN UN OBJETO DishEntity
                            DishEntity savedDishEntity = dataSnapshot.getValue(DishEntity.class);
                            // SI NO ES NULO
                            if(savedDishEntity != null) {
                                // CREAMOS EL OBJETO QUE MANDAREMOS AL SERVICE
                                DishResponse dishResponse = new DishResponse(
                                        savedDishEntity.getId(),
                                        savedDishEntity.getName(),
                                        savedDishEntity.getIngredients(),
                                        savedDishEntity.getAllergens(),
                                        savedDishEntity.getImage(),
                                        savedDishEntity.getType());
                                // Y LO MANDAMOS
                                callback.onSuccess(dishResponse);
                            }
                            else {
                                // SI NO, LO MANDAMOS VACÍO
                                DishResponse dishResponse = new DishResponse();
                                dishResponse.setId(0);
                                callback.onSuccess(dishResponse);
                            }
                        }
                        else {
                            callback.onFailure(new Exception("No se encontró el plato guardado en la base de datos"));
                        }
                    }

                    // MANDAMOS EXCEPCIÓN SI HAY UN ERROR
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        callback.onFailure(new Exception("Error al leer el plato guardado: " + databaseError.getMessage()));
                    }
                });
            }));
        });
    }


    /**
     * ACTUALIZA UN PLATO YA GUARDADO EN LA BASE DE DATOS
     * PARA HACERLO PRIMERO GUARDA LOS VALORES QUE TIENE GUARDADOS ACTUALMENTE PARA QUE LOS MANTENGA SI NO VIENEN EN EL OBJETO
     * @param dishEntity OBJETO QUE HAY QUE GUARDAR
     * @param callback SI EXITO: true, SI NO, false
     */
    public void update(DishEntity dishEntity, UpdateDishCallback callback){

        // GUARDAMOSLA REFERENCIA DEL PLATO QUE QUEREMOS ACTUALIZAR APUNTANDO AL UID QUE TRAE EL OBJETO DishEntity QUE VIENE POR PARÁMETRO
        DatabaseReference dishRef = databaseReference.child("restaurants")
                .child(dishEntity.getUid())
                .child("dishes/items")
                .child(String.valueOf(dishEntity.getId()));

        // ACCEDEMOS A ESA REFERENCIA
        dishRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // SI EXISTE ENTRAMOS
                if(dataSnapshot.exists()){
                    // OBTENEMOS TODOS LOS DATOS GUARDADOS ACTUALMENTE
                    Integer currentId = dataSnapshot.child("id").getValue(Integer.class);
                    String currentName = dataSnapshot.child("name").getValue(String.class);
                    String currentIngredients = dataSnapshot.child("ingredients").getValue(String.class);
                    String currentImage = dataSnapshot.child("image").getValue(String.class);
                    String currentType = dataSnapshot.child("type").getValue(String.class);
                    ArrayList<String> currentAllergens =  (ArrayList<String>) dataSnapshot.child("allergens").getValue();

                    // CREAMOS UN PLATO VACÍO
                    DishUpdateEntity dishEntityToBeSaved = new DishUpdateEntity();

                    // LE ASIGNAMOS EL VALOR QUE TRAE EL OBJETO ENTRANTE, Y SI NO TIENE ALGO LE AÑADIMOS EL VALOR QUE YA TENÍA GUARDADO EN REALTIME
                    dishEntityToBeSaved.setId(dishEntity.getId() != null && dishEntity.getId() != 0 ? dishEntity.getId() : currentId);
                    dishEntityToBeSaved.setName(dishEntity.getName() != null && !dishEntity.getName().isEmpty() ? dishEntity.getName() : currentName);
                    dishEntityToBeSaved.setIngredients(dishEntity.getIngredients() != null && !dishEntity.getIngredients().isEmpty() ? dishEntity.getIngredients() : currentIngredients);
                    dishEntityToBeSaved.setImage(dishEntity.getImage() != null && !dishEntity.getImage().isEmpty() ? dishEntity.getImage() : currentImage);
                    dishEntityToBeSaved.setType(dishEntity.getType() != null && !dishEntity.getType().isEmpty() ? dishEntity.getType() : currentType);
                    dishEntityToBeSaved.setAllergens(dishEntity.getAllergens() != null  ? dishEntity.getAllergens() : currentAllergens);

                    // AHORA ESE PLATO LO SOBREESCRIBIMOS EN REALTIME
                    dishRef.setValue(dishEntityToBeSaved, (databaseError, databaseReference1) -> {
                        // SI HAY ERROR
                        if(databaseError != null){
                            // MANDAMOS UNA EXCEPCIÓN AL SERVICE Y CON RETURN SALIMOS
                            callback.onFailure(new Exception("Error al guardar el plato" + databaseError.getMessage()));
                            return;
                        }

                        // SI SALE BIEN, ACCEDEMOS AL PLATO QUE ACABAMOS DE GUARDAR
                        dishRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // SI EXISTE
                                if(dataSnapshot.exists()){
                                    // MANDAMOS true AL SERVICE
                                    callback.onSuccess(true);
                                }
                                else{
                                    // SI NO, MANDAMOS false
                                    callback.onSuccess(false);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                callback.onFailure(new Exception("Error al leer el plato guardado: " + databaseError.getMessage()));
                            }
                        });
                    });
                }
            }

            // MANDAMOS EXCEPCIÓN SI HAY UN ERROR
            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
            }
        });
    }


    /**
     * DEVUELVE TODOS LOS PLATOS DEL RESTARUANTE QUE SE PASA POR PARÁMETRO
     * 1º COMPRUEBA QUE EL RESTAURANTE EXISTA CON UNA LLAMADA A existsByUid DE RestaurantService
     * 2º CUANDO TERMINA LA PETICIÓN Y TENEMOS RESPUESTA CONTINUAMOS AQUÍ
     *      SI EXISTE EL RESTAURANTE COMPRUEBA SI TIENE PLATOS.
     *      SI LOS TIENE, DEVUELVE UN DishAllResponse VÍA CALLBACK
     * @param uid UID DEL RESTAURANTE
     *
     * @param callback SI TIENE ÉXITO DEVUELVE UN ArrayList CON TODOS LOS PLATOS EN FORMATO DishAllResponse (O VACÍO)
     */
    public void getAll(String uid, FindAllDishesCallback callback) {

        // CREAMOS UN FUTURO QUE ALMACENARÁ SI EL RESTARUANTE EXISTE O NO
        CompletableFuture<Boolean> futureExists = restaurantService.existsByUid(uid);


        futureExists.thenAccept(exists -> {
            // SI EXISTE
            if (exists) {
                // GUARDAMOS LA REFERENCIA DEL NODO DE SUS PLATOS
                DatabaseReference dishesRef = databaseReference.child("restaurants")
                        .child(uid)
                        .child("dishes/items");

                // ACCEDEMOS AL NODO DE LOS PLATOS
                dishesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // SI EXISTE
                        if (dataSnapshot.exists()) {
                            // CREAMOS UN ARRAY PARA GUARDARLOS
                            ArrayList<DishResponse> allDishes = new ArrayList<>();

                            // LOS RECORREMOS TODOS
                            for (DataSnapshot dishSnapshot : dataSnapshot.getChildren()) {
                                // GUARDAMOS CADA PLATO EN UN OBJETO DishResponse
                                DishResponse dish = dishSnapshot.getValue(DishResponse.class);
                                // SI EL PLATO OBTENIDO EXISTE
                                if (dish != null) {
                                    // LO AÑADIMOS AL ARRAY
                                    allDishes.add(dish);
                                }
                            }
                            // CREAMOS EL OBJETO QUE QUEREMOS MANDAR
                            DishAllResponse dishAllResponse = new DishAllResponse();
                            // LE AÑADIMOS EL ARRAY DE PLATOS
                            dishAllResponse.setDishes(allDishes);
                            // LO MANDAMOS AL SERVICE
                            callback.onSuccess(dishAllResponse);
                        } else {
                            // SI SALE MAL MANDAMOS UN OBJETO CON UN ARRAY VACÍO
                            DishAllResponse emptyResponse = new DishAllResponse();
                            emptyResponse.setDishes(new ArrayList<>());
                            callback.onSuccess(emptyResponse);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // SI HAY UN ERROR MANDAMOS UNA EXCEPCIÓN
                        callback.onFailure(new Exception("Error al leer los platos: " + databaseError.getMessage()));
                    }
                });
            } else {
                DishAllResponse emptyResponse = new DishAllResponse();
                emptyResponse.setDishes(new ArrayList<>());
                callback.onSuccess(emptyResponse);
            }
        }).exceptionally(ex -> {
            callback.onFailure(new Exception("Error al verificar la existencia del restaurante: " + ex.getMessage()));
            return null;
        });
    }


    /**
     * ELIMINA UN PLATO DE LA BASE DE DATOS
     * @param uid UID DEL RESTAURANTE EN AUTHENTICATION
     * @param dishId ID DLE PLATO EN EL NODO DEL RESTAURANTE
     * @return DEVUELVE EN UN CompletableFuture True SI TUVO ÉXITO O false SI NO LO TUVO
     */
    public CompletableFuture<Boolean> delete(String uid, int dishId) {
        // CREAMOS UN FUTURO QUE PASAREMOS AL SERVICE
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // GUARDAMOS REFERENCIA DEL PLATO CON EL ID APORTADO
        DatabaseReference dishRef = databaseReference.child("restaurants")
                .child(uid)
                .child("dishes/items")
                .child(String.valueOf(dishId));

        // ACCEDEMOS A ESE PLATO
        dishRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // SI EXISTE ENTRAMOS
                if (dataSnapshot.exists()) {
                    // Y LO ELIMINAMOS DE BASE DE DATOS
                    dishRef.removeValue((databaseError, databaseReference1) -> {
                        // SI HAY UN ERROR AL HACERLO
                        if (databaseError != null) {
                            // COMPLETAMOS EL FUTURO CON UNA EXCEPCIÓN
                            future.completeExceptionally(databaseError.toException());
                        } else {
                            // SU SALE BIEN LO COMPLETAMOS CON UN true
                            future.complete(true);
                        }
                    });
                } else {
                    // SI NO LO ENCONTRAMOS LO COMPLETAMOS CON UN false
                    future.complete(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // SI HAY UN EROR DE CONEXIÓN DEVOLVEMOS UNA EXCEPCIÓN
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }


    /**
     * RECUPERA DE LA BSE DE DATOS UN OBJETO DishEntity
     * @param uid  UID DEL RESTAURANTE EN AUTHENTICATION
     * @param id   ID DEL PLATO EN EL RESTAURANTE
     * @param callback SI ÉXITO DEVUELVE UN OBJETO DishEntity
     *                 SI NO EXITO DEVUELVE UN DishEntity vacío
     */
    public void get(String uid, int id, OnDishGotCallback callback){
        // REFERNCIA DEL RESTAURANTE
        DatabaseReference restaurantRef = databaseReference.child("restaurants").child(uid);
        // REFERENCIA DE LOS PLATOS DEL RESTAURANTE
        DatabaseReference dishesRef = restaurantRef.child("dishes/items");

        // ACCEDEMOS AL NODO DE LOS PLATOS
        dishesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // SI EXISTEN
                if(dataSnapshot.exists()){
                    // GUARDAMOS EL PLATO CON EL ID APORTADO EN UN OBJETO DishEntity
                    DishEntity dishEntity = dataSnapshot.child(String.valueOf(id)).getValue(DishEntity.class);
                    // MANDAMOS EL DishEntity AL SERVICE
                    callback.onSuccess(dishEntity);
                }
            }

            // SI HAY UN ERROR MANDAMOS UN DishEntity VACÍO AL SERVICE
            @Override
            public void onCancelled(DatabaseError databaseError) {
                    callback.onSuccess(new DishEntity());
            }
        });

    }


    public interface SavePlatoCallback{
        void onSuccess(DishResponse dishResponse);
        void onFailure(Exception exception);
    }


    public interface FindPlatoIdCallback{
        void onSuccess(Integer id);
        void onFailure(DatabaseError exception, Integer id);
    }


    public interface UpdateDishCallback{
        void onSuccess(Boolean result);
        void onFailure(Exception exception);
    }


    public interface FindAllDishesCallback{
        void onSuccess(DishAllResponse allDish);
        void onFailure(Exception exception);
    }


    public interface OnDishGotCallback{
        void onSuccess(DishEntity dishEntity);
        void onFailure(Exception exception);
    }


}
