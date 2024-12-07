package com.example.homiyummy.service;

import com.example.homiyummy.model.dish.*;
import com.example.homiyummy.model.menu.MenuGetByNumEntity;
import com.example.homiyummy.model.menu.MenuGetByNumResponse;
import com.example.homiyummy.model.menu.MenuInGetTasksResponse;
import com.example.homiyummy.model.order.*;
import com.example.homiyummy.model.user.UserInGetTasksResponse;
import com.example.homiyummy.repository.OrderRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository){
        this.orderRepository = orderRepository;
    }


    // ----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<Integer> findLastId(String uid){

        CompletableFuture<Integer> futureId = new CompletableFuture<Integer>();

        orderRepository.findId(uid, new OrderRepository.OnOrderIdGot() {
            @Override
            public void onFindingSuccess(int actualId) {futureId.complete(actualId);}

            @Override
            public void onFindingFailure(Exception exception) {
                futureId.completeExceptionally(exception);
            }
        });

        return futureId;
    }

    // ----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<OrderCreatedResponse> createOrder(OrderDTO orderDTO, int lastOrderId){

        int newOrderId = lastOrderId + 1;
        CompletableFuture<OrderCreatedResponse> futureOrder = new CompletableFuture<>();

        orderRepository.save(orderDTO, newOrderId, new OrderRepository.OnSavingOrderCallback() {
            @Override
            public void onSavingOrderSuccess(OrderCreatedResponse orderCreatedResponse) {
                futureOrder.complete(orderCreatedResponse);
            }

            @Override
            public void onSavingOrderFailure(Exception exception) {
                futureOrder.completeExceptionally(exception);
            }
        });
        return futureOrder;
    }

    // ----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<OrderGotByNumResponse> getRestaurantData (String orderNumber){

        CompletableFuture<OrderGotByNumResponse> futureRestData = new CompletableFuture<>();

        orderRepository.getOrderDataInRestaurantSide(orderNumber, new OrderRepository.OnOrderGotCallback() {
            @Override
            public void onFindingSuccess(OrderGotByNumEntity orderGotByNumEntity) {

                OrderGotByNumResponse orderResponse = new OrderGotByNumResponse();

                orderResponse.setUid(orderGotByNumEntity.getUid());
                orderResponse.setDate(orderGotByNumEntity.getDate());
                orderResponse.setUidCustomer(orderGotByNumEntity.getUidCustomer());

                ArrayList<MenuGetByNumResponse> allMenusResponse = new ArrayList<>();

                for(MenuGetByNumEntity menuEntity : orderGotByNumEntity.getMenus()){

                    int menuId = menuEntity.getId();
                    int menuDate = menuEntity.getDate();
                    float menuPrice = menuEntity.getPrice();
                    String menuStatus = menuEntity.getStatus();

                    DishGetByResponse firstCourseResponse = new DishGetByResponse(
                            menuEntity.getFirst_course().getId(),
                            menuEntity.getFirst_course().getName(),
                            menuEntity.getFirst_course().getIngredients(),
                            menuEntity.getFirst_course().getAllergens(),
                            menuEntity.getFirst_course().getImage());


                    DishGetByResponse secondCourseResponse = new DishGetByResponse(
                            menuEntity.getSecond_course().getId(),
                            menuEntity.getSecond_course().getName(),
                            menuEntity.getSecond_course().getIngredients(),
                            menuEntity.getSecond_course().getAllergens(),
                            menuEntity.getSecond_course().getImage());

                    if(menuEntity.getDessert().getName() != null){
                        DishGetByResponse dessertResponse = new DishGetByResponse(menuEntity.getDessert().getId(), menuEntity.getDessert().getName(), menuEntity.getDessert().getIngredients(),menuEntity.getDessert().getAllergens(),menuEntity.getDessert().getImage());
                        MenuGetByNumResponse menuResponse = new MenuGetByNumResponse(
                                menuId, menuDate, firstCourseResponse, secondCourseResponse, dessertResponse, menuPrice, menuStatus
                        );
                        allMenusResponse.add(menuResponse);
                    }
                    else {
                        Map<String, Object> emptyDessert = new HashMap<>(); // Objeto vacío

                        MenuGetByNumResponse menuResponse = new MenuGetByNumResponse(
                                menuId, menuDate, firstCourseResponse, secondCourseResponse, emptyDessert, menuPrice, menuStatus
                        );
                        allMenusResponse.add(menuResponse);
                    }

                }
                orderResponse.setMenus(allMenusResponse);
                orderResponse.setStatus(orderGotByNumEntity.getStatus());
                orderResponse.setTotal(orderGotByNumEntity.getTotal());

                futureRestData.complete(orderResponse);
            }

            @Override
            public void onFindingFailure(Exception exception) {
                futureRestData.completeExceptionally(exception);
            }
        });
        return futureRestData;
    }

    // ----------------------------------------------------------------------------------------------------------------

    public CompletableFuture< ArrayList<OrderGetClientOrdersResponse>> getClientOrders(String clientUID){

        CompletableFuture< ArrayList<OrderGetClientOrdersResponse>> futureResponse = new CompletableFuture<>();

        orderRepository.getClientOrders(clientUID, new OrderRepository.OnClientOrdersGotCallback() {
            @Override
            public void onFindingSuccess(ArrayList<OrderGetClientOrdersEntity> ordersEntity) {

                ArrayList<OrderGetClientOrdersResponse> allOrders = new ArrayList<>();

                for(OrderGetClientOrdersEntity order: ordersEntity ){

                    OrderGetClientOrdersResponse orderResponse = new OrderGetClientOrdersResponse();

                    orderResponse.setName_restaurant(order.getName_restaurant());
                    orderResponse.setImage_restaurant(order.getImage_restaurant());
                    orderResponse.setDate(order.getDate());
                    orderResponse.setNum_order(order.getNum_order());
                    orderResponse.setTotal(order.getTotal());
                    orderResponse.setStatus(order.getStatus());
                    allOrders.add(orderResponse);
                }

                futureResponse.complete(allOrders);
            }

            @Override
            public void onFindingFailure(Exception exception) {
                futureResponse.completeExceptionally(exception);
            }
        });

        return futureResponse;
    }

    // ----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<ArrayList<OrderGetRestaurantOrdersResponse>> getRestOrders(String restUID) {

        CompletableFuture<ArrayList<OrderGetRestaurantOrdersResponse>> futureResponse = new CompletableFuture<>();

        orderRepository.getAllOrdersInARestaurant(restUID, new OrderRepository.OnRestaurantOrdersGotCallback() {
            @Override
            public void onFindingSuccess(ArrayList<OrderGetRestaurantOrdersEntity> ordersArrayEntity) {

                ArrayList<OrderGetRestaurantOrdersResponse> response = new ArrayList<>();

                for (OrderGetRestaurantOrdersEntity entity : ordersArrayEntity) {
                    OrderGetRestaurantOrdersResponse ordersResponse = new OrderGetRestaurantOrdersResponse();

                    ordersResponse.setName_client(entity.getName_client());
                    ordersResponse.setDate(entity.getDate());
                    ordersResponse.setNum_order(entity.getNum_order());
                    ordersResponse.setTotal(entity.getTotal());
                    ordersResponse.setNum_menus(entity.getNum_menus());
                    ordersResponse.setStatus(entity.getStatus());

                    response.add(ordersResponse);
                }

                futureResponse.complete(response);
            }

            @Override
            public void onFindingFailure(Exception exception) {
                futureResponse.completeExceptionally(exception);
            }
        });

        return futureResponse;
    }

    // ----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<ArrayList<OrderGetTasksResponse>> getTasks(OrderGetTasksRequest request){

        CompletableFuture<ArrayList<OrderGetTasksResponse>> future = new CompletableFuture<>();

        ArrayList<OrderGetTasksResponse> tasksResponse = new ArrayList<>();

        orderRepository.getDaylyTask(request, new OrderRepository.OnDaylyTaskFindingCallback() {
            @Override
            public void onFindingSuccess(ArrayList<OrderGetTasksEntity> tasksEntity) {

                for(OrderGetTasksEntity task : tasksEntity){

                    // CREAMOS EL ID
                    String menuIdResponse = task.getNum_order();

                    // CREAMOS EL MENÚ
                    MenuInGetTasksResponse menuResponse = new MenuInGetTasksResponse();
                    menuResponse.setId(task.getMenu().getId());
                    menuResponse.setDate(task.getMenu().getDate());

                    DishGetDayTaskResponse firstResponse = new DishGetDayTaskResponse();
                    firstResponse.setId(task.getMenu().getFirst_course().getId());
                    firstResponse.setName(task.getMenu().getFirst_course().getName());
                    firstResponse.setIngredients(task.getMenu().getFirst_course().getIngredients());
                    firstResponse.setAllergens(task.getMenu().getFirst_course().getAllergens());
                    firstResponse.setImage(task.getMenu().getFirst_course().getImage());

                    DishGetDayTaskResponse secondResponse = new DishGetDayTaskResponse();
                    secondResponse.setId(task.getMenu().getSecond_course().getId());
                    secondResponse.setName(task.getMenu().getSecond_course().getName());
                    secondResponse.setIngredients(task.getMenu().getSecond_course().getIngredients());
                    secondResponse.setAllergens(task.getMenu().getSecond_course().getAllergens());
                    secondResponse.setImage(task.getMenu().getSecond_course().getImage());

                    DishGetDayTaskResponse dessertResponse = new DishGetDayTaskResponse();
                    dessertResponse.setId(task.getMenu().getDessert().getId());
                    dessertResponse.setName(task.getMenu().getDessert().getName());
                    dessertResponse.setIngredients(task.getMenu().getDessert().getIngredients());
                    dessertResponse.setAllergens(task.getMenu().getDessert().getAllergens());
                    dessertResponse.setImage(task.getMenu().getDessert().getImage());

                    menuResponse.setFirst_course(firstResponse);
                    menuResponse.setSecond_course(secondResponse);
                    menuResponse.setDessert(dessertResponse);
                    menuResponse.setStatus(task.getMenu().getStatus());

                    // CREAMOS USUARIO
                    UserInGetTasksResponse userResponse = new UserInGetTasksResponse();
                    userResponse.setName(task.getCustomer().getName());
                    userResponse.setSurname(task.getCustomer().getSurname());
                    userResponse.setPhone(task.getCustomer().getPhone());
                    userResponse.setEmail(task.getCustomer().getEmail());
                    userResponse.setAllergens(task.getCustomer().getAllergens());

                    OrderGetTasksResponse orderResponse = new OrderGetTasksResponse(menuIdResponse, menuResponse, userResponse);

                    tasksResponse.add(orderResponse);
                }
            future.complete(tasksResponse);
            }

            @Override
            public void onFindingFailure(Exception exception) {
                future.completeExceptionally(exception);
            }
        });

        return future;
    }

    // ----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<OrderUpdateStatusResponse> updateMenu (OrderUpdateStatusRequest request){
        CompletableFuture<OrderUpdateStatusResponse> future = new CompletableFuture<>();
        orderRepository.updateMenu(request, new OrderRepository.OnStatusFindingCallback() {
            @Override
            public void onFindingSuccess(Boolean result) {
                OrderUpdateStatusResponse response = new OrderUpdateStatusResponse();
                response.setChange(result);
                future.complete(response);
            }

            @Override
            public void onFindingFailure(Exception exception) {
                future.completeExceptionally(exception);
            }
        });
        return future;
    }

    // ----------------------------------------------------------------------------------------------------------------

}
