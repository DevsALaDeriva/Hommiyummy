package com.example.homiyummy.service;

import com.example.homiyummy.model.order.*;
import com.example.homiyummy.model.restaurant.RestaurantGetByOrderNumberEntity;
import com.example.homiyummy.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
        OrderEntity orderEntity = new OrderEntity();

        orderEntity.setUid(orderDTO.getUid());
        orderEntity.setNumOrder(orderDTO.getNumOrder());
        orderEntity.setDate(orderDTO.getDate());
        orderEntity.setCustomerUid(orderDTO.getCustomerUid());
        orderEntity.setMenus(orderDTO.getMenus());
        orderEntity.setTotal(orderDTO.getTotal());

        orderRepository.save(orderEntity, newOrderId, new OrderRepository.OnSavingOrderCallback() {
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

    public CompletableFuture<OrderWithRestaurantDataEntity> getRestaurantData (String uid){

        CompletableFuture<OrderWithRestaurantDataEntity> futureRestData = new CompletableFuture<>();

        orderRepository.getOrderDataInRestaurantSide(uid, new OrderRepository.OnOrderGotCallback() {
            @Override
            public void onFindingSuccess(OrderWithRestaurantDataEntity orderWithRestaurantDataEntity) {
                futureRestData.complete(orderWithRestaurantDataEntity);
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

                OrderGetClientOrdersResponse orderResponse = new OrderGetClientOrdersResponse();
                ArrayList<OrderGetClientOrdersResponse> allOrders = new ArrayList<>();

                for(OrderGetClientOrdersEntity order: ordersEntity ){
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
                //System.out.println("Cantidad de entidades recibidas del repositorio: " + ordersArrayEntity.size());

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

                //System.out.println("Servicio - Tamaño del array antes de completar futureResponse: " + response.size());
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

}
