package com.example.homiyummy.repository;

import com.example.homiyummy.model.dish.DishEntity;
import com.example.homiyummy.model.dish.DishInOrderEntity;
import com.example.homiyummy.model.menu.MenuEntity;
import com.example.homiyummy.model.menu.MenuGetByNumEntity;
import com.example.homiyummy.model.order.*;
import com.example.homiyummy.model.restaurant.RestaurantGetByOrderNumberEntity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class OrderRepository {

    DatabaseReference databaseReference;

    public OrderRepository(DatabaseReference databaseReference){
        this.databaseReference = databaseReference;
    }


    // ----------------------------------------------------------------------------------------------------------------
    // -> OK REVISITÓN DE CALLBACKS Y EXCEPCIONES

    /**
     * La 1ª vez que se compruebe si un Id existe, crea el nodo "counter",si no existe de antes.
     * Con dataSnapshot.exists() lo que realmente se comprueba es si este nodo contiene datos, no si existe.
     * Por eso, la primera vez devuelve false y salta al else, en el que asignamos 0.
     *
     * @param callback: usamos la versión exitosa cuando:
     *                              - encuentra un dato guardado en el nodo
     *                              - y cuando no lo encuentra (que asigna 0)
     *                  usamos la versión errónea cuando:
     *                              - se encuentra como dato un null
     *                              - o cuando directamente ha entrado en onCancelled, por un error de conexión, mala configuración del acceso...
     *
     *  @param uid el identificador único del restaurante
     */

    public void findId(String uid, OnOrderIdGot callback) {

        DatabaseReference restaurantRef = databaseReference.child("restaurants").child(uid);
        DatabaseReference ordersRef = restaurantRef.child("orders");
        DatabaseReference counterRef = ordersRef.child("counter");       //----> ESTO SOLO CREA LA UBICACIÓN

        counterRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){                                        //----> LA 1º VEZ QUE SE VAYA A USAR DARÁ FALSE PQ ESA UBICACIÓN RECIÉN CREADA ESTÁ VACÍA
                    Integer actualCounter = dataSnapshot.getValue(Integer.class);
                    if(actualCounter != null) {
                        callback.onFindingSuccess(actualCounter);
                    }
                    else{
                        callback.onFindingFailure(new Exception("El valor del contador en Realtime es null"));
                    }
                }
                else{
                    System.out.println("Ha dado false pq, al ser la 1ª vez, el nodo se acaba de crear y no tiene datos");
                    callback.onFindingSuccess(0);                              //----> LA 1ª VEZ, SALTA AQUÍ Y LE ASIGNAMOS UN 0
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFindingFailure(databaseError.toException());
            }
        });
    }

    // ----------------------------------------------------------------------------------------------------------------
    // -> OK REVISITÓN DE CALLBACKS Y EXCEPCIONES

    public void save(OrderDTO orderDTO, int newId, OnSavingOrderCallback callback){

        OrderEntity orderEntity = new OrderEntity();

        orderEntity.setNum_order(orderDTO.getNumOrder());
        orderEntity.setDate(orderDTO.getDate());
        orderEntity.setUidCustomer(orderDTO.getCustomerUid());
        orderEntity.setMenus(orderDTO.getMenus());
        orderEntity.setTotal(orderDTO.getTotal());

        DatabaseReference restaurantRef = databaseReference.child("restaurants").child(orderDTO.getUid());
        DatabaseReference allOrdersRef = restaurantRef.child("orders");
        DatabaseReference counterRef = allOrdersRef.child("counter");
        DatabaseReference itemsRef = allOrdersRef.child("items");
        DatabaseReference thisOrderRef = itemsRef.child(String.valueOf(newId)); // A LA VEZ QUE CREAMOS EL NODO DEL NUEVO PEDIDO, LO REFERENCIAMOS

        counterRef.setValue(newId, (databaseCounterError, databaseCounterReference) -> {
            if(databaseCounterError != null){
                callback.onSavingOrderFailure(new Exception("Error al actualizar el contador de los pedidos: " + databaseCounterError.getMessage()));
                return;
            }

            counterRef.addListenerForSingleValueEvent(new ValueEventListener() {// NOS DIRIGIMOS A COUNTER, PQ QUIERO LEER EL VALOR QUE ACABO DE GRABAR EN COUNTER
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Integer orderId = dataSnapshot.getValue(Integer.class); // OBTENEMOS EL VALOR QUE ACABAMOS DE GUARDAR PARA ASEGURARNOS Q USAMOS EL CORRECTO
                    if(orderId != null){
                        thisOrderRef.setValue(orderEntity, (databaseOrderError, databaseOrderReference) -> {
                            if(databaseOrderError != null){
                                callback.onSavingOrderFailure(new Exception("Error al guardar el pedido: " + databaseOrderError.getMessage()));
                                return;
                            }

                            OrderCreatedResponse orderCreatedResponse = new OrderCreatedResponse(orderId);
                            callback.onSavingOrderSuccess(orderCreatedResponse);
                        });
                    } else {
                        callback.onSavingOrderFailure(new Exception("El valor del contador es nulo"));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    callback.onSavingOrderFailure(databaseError.toException());
                }
            });
        });

    }

    // ----------------------------------------------------------------------------------------------------------------

    public interface OnOrderIdGot{
        void onFindingSuccess(int id);
        void onFindingFailure(Exception exception);
    }

    // ----------------------------------------------------------------------------------------------------------------

    public interface OnSavingOrderCallback{
        void onSavingOrderSuccess(OrderCreatedResponse orderCreatedResponse);
        void onSavingOrderFailure(Exception exception);
    }

    // ----------------------------------------------------------------------------------------------------------------

    public void getOrderDataInRestaurantSide(String orderNumber, OnOrderGotCallback callback){

        DatabaseReference allRestaurantsRef = databaseReference.child("restaurants");
        allRestaurantsRef.addListenerForSingleValueEvent(new ValueEventListener() {               // TODOS LOS RESTAURANTES
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot restNode : dataSnapshot.getChildren()){            // RECORREMOS TODOS LOS RESTAURANTES
                        DataSnapshot ordersRef = restNode.child("orders/items");   // ENTRAMOS EN LOS PEDIDOS DE CADA RESTAURANTE SI LOS TIENE
                        if(ordersRef.exists()){

                            for(DataSnapshot order : ordersRef.getChildren()){
                                System.out.println("Child key: " + order.getKey() + ", value: " + order.getValue());
                                if(order.child("num_order").getValue(String.class).equals(orderNumber)){
                                    //String restUID = restNode.getKey();                             // GUARDAMOS EL UID DEL RESTAURANTE QUE TIENE EL PEDIDO
                                    String customerUID = order.child("uidCustomer").getValue(String.class); // GUARDAMOS EL UID DEL CLIENTE
                                    //System.out.println("UID del usuario ------------: " + customerUID);
                                    String restName = restNode.child("name").getValue(String.class); // GUARDAMOS NOMBRE DEL RESTAURANTE
                                    int date = order.child("date").getValue(Integer.class); // GUARDAMOS FECHA DEL PEDIDO
                                    //System.out.println("date ------------: " + date);

                                    float totalOrder = order.child("total").getValue(Float.class); // GUARDAMOS TOTAL DEL PEDIDO

                                    DataSnapshot menusSnapshot = order.child("menus");

                                    if(menusSnapshot.exists()){

                                        ArrayList<MenuGetByNumEntity> menusInOrder = new ArrayList<>();

                                        for(DataSnapshot menu : menusSnapshot.getChildren()){

                                            int dateM = order.child("date").getValue(Integer.class);
                                            int firsCourseID = menu.child("first_course").getValue(Integer.class);
                                            int secondCourseID = menu.child("second_course").getValue(Integer.class);
                                            int dessertID = menu.child("dessert").getValue(Integer.class);
                                            float totalMenu = menu.child("price").getValue(Float.class);
                                            String status = menu.child("status").getValue(String.class);
                                            DishInOrderEntity wholeFirstCourse = new DishInOrderEntity();
                                            DishInOrderEntity wholeSecondCourse = new DishInOrderEntity();
                                            DishInOrderEntity wholeDessert = new DishInOrderEntity();

                                            // OBTENEMOS LOS PLATOS CORRESPONDIENTES A CADA NÚMERO
                                            DataSnapshot dishesSnapshot = restNode.child("dishes/items");
                                            if(dishesSnapshot.exists()){ // SI EXISTE EL NODO PLATOS

                                                DataSnapshot firstCourseRef = dishesSnapshot.child(String.valueOf(firsCourseID));

                                                if(firstCourseRef.exists()){ // LLENAMOS EL VALOR DEL PRIMER PLATO
                                                    String nameD = firstCourseRef.child("name").getValue(String.class);
                                                    String ingredientsD = firstCourseRef.child("ingredients").getValue(String.class);
                                                    String imageD = firstCourseRef.child("image").getValue(String.class);
                                                    String allergensD = firstCourseRef.child("allergens/0").getValue(String.class);
                                                    wholeFirstCourse = new DishInOrderEntity(nameD, ingredientsD, allergensD, imageD);
                                                }

                                                DataSnapshot secondCourseRef = dishesSnapshot.child(String.valueOf(secondCourseID));

                                                if(secondCourseRef.exists()){ // LLENAMOS EL VALOR DEL SEGUNDO PLATO
                                                    String nameD = secondCourseRef.child("name").getValue(String.class);
                                                    String ingredientsD = secondCourseRef.child("ingredients").getValue(String.class);
                                                    String imageD = secondCourseRef.child("image").getValue(String.class);
                                                    String allergensD = secondCourseRef.child("allergens/0").getValue(String.class);
                                                    wholeSecondCourse = new DishInOrderEntity(nameD, ingredientsD, allergensD, imageD);
                                                }

                                                DataSnapshot dessertCourseRef = dishesSnapshot.child(String.valueOf(dessertID));

                                                if(dessertCourseRef.exists()){ // LLENAMOS EL VALOR DEL POSTRE
                                                    String nameD = dessertCourseRef.child("name").getValue(String.class);
                                                    String ingredientsD = dessertCourseRef.child("ingredients").getValue(String.class);
                                                    String imageD = dessertCourseRef.child("image").getValue(String.class);
                                                    String allergensD = dessertCourseRef.child("allergens/0").getValue(String.class);
                                                    wholeDessert = new DishInOrderEntity(nameD, ingredientsD, allergensD, imageD);
                                                }
                                            }

                                            MenuGetByNumEntity singleMenu = new MenuGetByNumEntity(date, wholeFirstCourse,wholeSecondCourse,wholeDessert,totalMenu, status); // CREAMOS CADA MENU
                                            menusInOrder.add(singleMenu); // Y LO METEMOS DENTRO DEL ARRAY DE MENÚS DEL PEDIDO
                                        }

                                        OrderWithRestaurantDataEntity dataEntity =
                                                new OrderWithRestaurantDataEntity(restName, orderNumber, date, menusInOrder, totalOrder, customerUID);

                                        //System.out.println("Nombre restaurante: " + dataEntity.getName_restaurant());
                                       // System.out.println("Número de pedido: " + dataEntity.getNum_order());
                                        //System.out.println("Fecha: " + dataEntity.getDate());
                                       // System.out.println("Cantidad de menús: " + dataEntity.getMenus());
                                        //System.out.println("Total: " +dataEntity.getTotal());
                                        //System.out.println("UID del usuario: " + dataEntity.getCustomerUID());

                                        callback.onFindingSuccess(dataEntity);
                                    }
                                }
                            }
                        }
                    }
                    callback.onFindingFailure(new Exception("No existe ese número de pedido."));
                }
                else {
                    callback.onFindingFailure(new Exception("No se han encontrado restaurantes en la base de datos."));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                    callback.onFindingFailure(databaseError.toException());
            }
        });

    }

    // ----------------------------------------------------------------------------------------------------------------

    public interface OnOrderGotCallback{
        void onFindingSuccess(OrderWithRestaurantDataEntity orderEntity);
        void onFindingFailure(Exception exception);
    }

    // ----------------------------------------------------------------------------------------------------------------



}
