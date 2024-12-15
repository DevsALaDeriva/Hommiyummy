package com.example.homiyummy.service;

import com.example.homiyummy.model.order.OrderGotByNumResponse;
import com.example.homiyummy.model.order.OrderWithRestaurantDataEntity;
import com.example.homiyummy.model.user.*;
import com.example.homiyummy.repository.UserRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference bbddRef;

    public UserService(FirebaseAuth firebaseAuth, FirebaseApp firebaseApp){
        bbddRef = FirebaseDatabase.getInstance().getReference(); // INYECTA LA REFERENCIA DE LA BBDD
        this.firebaseAuth = firebaseAuth;
    }

    // ------------------------------------------------------------------------------------------------------------
    public UserResponse createUser(UserDTO userDTO) {

        UserEntity userEntity = new UserEntity();

        userEntity.setUid(userDTO.getUid());
        userEntity.setName(userDTO.getName());
        userEntity.setAddress(userDTO.getAddress());
        userEntity.setCity(userDTO.getCity());
        userEntity.setSurname(userDTO.getSurname());
        userEntity.setEmail(userDTO.getEmail());
        userEntity.setPhone(userDTO.getPhone());
        userEntity.setAllergens(userDTO.getAllergens());

        CompletableFuture<UserResponse> futureResponse = new CompletableFuture<>();

        userRepository.saveUser(userEntity, new UserRepository.SaveUserCallback() {
            @Override
            public void onSuccess(UserEntity userEntity) {
                UserResponse userResponse = new UserResponse();
                userResponse.setUid(userEntity.getUid());
                userResponse.setName(userEntity.getName());
                userResponse.setSurname(userEntity.getSurname());
                userResponse.setEmail(userEntity.getEmail());
                userResponse.setAddress(userEntity.getAddress());
                userResponse.setCity(userEntity.getCity());
                userResponse.setPhone(userEntity.getPhone());
                userResponse.setAllergens(userEntity.getAllergens());
                futureResponse.complete(userResponse);
            }
            @Override
            public void onFailure(Exception exception) {
                futureResponse.completeExceptionally(exception);
            }
        });

        try {
            return futureResponse.get(); // ESTO BLOQUEA EL CÓDIGO HASTA QUE ESTÉ DISPONIBLE EL DATO ANTES DE DEVOLVER
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar el usuario en Firebase", e);
        }

    }

// ------------------------------------------------------------------------------------------------------------

    public CompletableFuture<Boolean> updateUser(UserDTO userDTO)  {

        CompletableFuture<Boolean> futureBoolean = new CompletableFuture<>();

        UserEntity userEntity = new UserEntity();
        userEntity.setUid(userDTO.getUid());
        userEntity.setName(userDTO.getName());
        userEntity.setSurname(userDTO.getSurname());
        userEntity.setPhone(userDTO.getPhone());
        userEntity.setAllergens(userDTO.getAllergens());

        userRepository.updateUserData(userEntity, new UserRepository.GetUpdateConfirmationCallback() { // PASAMOS UNA IMPLEMENTACIÓN ANÓNIMA DE LA INTERFAZ AL REPOSITORIO
            @Override
            public void onSuccess(Boolean confirmation) {
                futureBoolean.complete(confirmation);
            }
            @Override
            public void onFailure(Exception exception) {
                futureBoolean.completeExceptionally(exception);
            }
        });
        try {
            return futureBoolean;
        }
        catch (Exception e){
            throw new RuntimeException("Error al obtener confirmación de la actualización del usuario", e);
        }
    }

// ----------------------------------------------------------------------------------------------------------------

    /**
     * SERVICIO CREADO SOLO PARA IDENTIFICAR SI UN USUARIO ES user o restaurant.
     * LE LLAMARÁ getUserTypeByUid DESDE UserTypeService PQ HAY QUE HACER UNA CONSULTA SIN SABER DE ENTRADA A CUÁL DE LOS DOS SERVICIOS SE LO MANDAMOS.
     * @param uid el uid del usuario en Authentication
     * @return el boolean que trae del repositorio
     */

    public CompletableFuture<Boolean> existsByUid(String uid) {
        return userRepository.existsByUid(uid);
    }

// ----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<UserReadResponse> findUserByUid(String uid){

        CompletableFuture<UserReadResponse> futureUser = new CompletableFuture<>();

        userRepository.find(uid, new UserRepository.FindUserCallback() {
            @Override
            public void onSuccess(UserFindEntity userFindEntity) {
                UserReadResponse userReadResponse = new UserReadResponse();

                userReadResponse.setEmail(userFindEntity.getEmail());
                userReadResponse.setName(userFindEntity.getName());
                userReadResponse.setSurname(userFindEntity.getSurname());
                userReadResponse.setAddress(userFindEntity.getAddress());
                userReadResponse.setCity(userFindEntity.getCity());
                userReadResponse.setPhone(userFindEntity.getPhone());
                userReadResponse.setAllergens(userFindEntity.getAllergens());

                futureUser.complete(userReadResponse);
            }
            @Override
            public void onFailure(Exception exception) {
                futureUser.completeExceptionally(exception);
            }
        });
        return futureUser;
    }

// ----------------------------------------------------------------------------------------------------------------


}
