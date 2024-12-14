package com.example.homiyummy.repository;

import com.example.homiyummy.model.user.UserEntity;
import com.example.homiyummy.model.user.UserFindEntity;
import com.example.homiyummy.model.user.UserReadResponse;
import com.example.homiyummy.model.user.UserResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class UserRepository {

    @Autowired
    private FirebaseDatabase firebaseDatabase;
    @Autowired
    private FirebaseAuth firebaseAuth;

    DatabaseReference databaseReference;

    public UserRepository( DatabaseReference databaseReference){
        this.databaseReference = databaseReference;
    }

    // ------------------------------------------------------------------------------------------------------------

    public void saveUser(UserEntity userEntity, SaveUserCallback callback)  {

        DatabaseReference userRef = firebaseDatabase.getReference("users").child(userEntity.getUid());

        userRef.setValue(userEntity, (databaseError, databaseReference) -> {
            if (databaseError == null) {
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {               // CONFIRMADO QUE NO HAY ERROR, LEEMOS LOS DATOS RECIÉN GUARDADOS
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        UserEntity recordedEntity = new UserEntity();
                        recordedEntity.setUid(dataSnapshot.child("uid").getValue(String.class));
                        recordedEntity.setName(dataSnapshot.child("name").getValue(String.class));
                        recordedEntity.setSurname(dataSnapshot.child("surname").getValue(String.class));
                        recordedEntity.setEmail(dataSnapshot.child("email").getValue(String.class));
                        recordedEntity.setAddress(dataSnapshot.child("address").getValue(String.class));
                        recordedEntity.setCity(dataSnapshot.child("city").getValue(String.class));
                        recordedEntity.setPhone(dataSnapshot.child("phone").getValue(String.class));
                        ArrayList<String> allergens = new ArrayList<>();
                        if(dataSnapshot.child("allergens").exists()){
                            DataSnapshot allergensSnapshot = dataSnapshot.child("allergens");
                            for(DataSnapshot allergen : allergensSnapshot.getChildren()){
                                allergens.add(allergen.getValue(String.class));
                            }
                            recordedEntity.setAllergens(allergens);
                        }
                        callback.onSuccess(recordedEntity);                                       // DEVOLVEMOS ESE OBJETO COMO PARÁMETRO DEL SEGUNDO CALLBACK (DE NUESTRA INTERFACE) SI ES EXITOSO
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        callback.onFailure(databaseError.toException());                        // EJECUTAMOS EL CALLBACK DE NUESTRA INTERFACE SI DA ERROR
                    }
                });
            } else {
                callback.onFailure(databaseError.toException());
            }
        });
    }

// ------------------------------------------------------------------------------------------------------------

    public interface SaveUserCallback {
        void onSuccess(UserEntity userEntity);
        void onFailure(Exception exception);
    }
// ------------------------------------------------------------------------------------------------------------

    public void updateUserData(UserEntity userEntity, GetUpdateConfirmationCallback callback) { // IMPLEMENTA LA INTERFAZ QUE LE SERVIRÁ AL SERVICIO PARA OBTENER LA CONFIRMACIÓN DEL ÉXITO O FALLO DE LA ACTUALIZACIÓN

        DatabaseReference userRef = firebaseDatabase.getReference("users").child(userEntity.getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() { // PRIMERO VER EL CONTENIDO GUARDADO EN REALTIME DEL USUARIO ANTES DE GUARDAR EL NUEVO
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        String currentName = dataSnapshot.child("name").getValue(String.class);
                        String currentSurname = dataSnapshot.child("surname").getValue(String.class);
                        String currentEmail = dataSnapshot.child("email").getValue(String.class);
                        String currentPhone = dataSnapshot.child("phone").getValue(String.class);
                        String currentAddress = dataSnapshot.child("address").getValue(String.class);
                        String currentCity = dataSnapshot.child("city").getValue(String.class);

                        ArrayList<String> currentAllergens = new ArrayList<>();
                        if(dataSnapshot.child("allergens").exists()){
                            DataSnapshot allergensSnapshot = dataSnapshot.child("allergens");
                            for(DataSnapshot allergen : allergensSnapshot.getChildren()){
                                currentAllergens.add(allergen.getValue(String.class));
                            }
                        }

                        UserEntity userEntityToBeSaved = new UserEntity(); // PODRÍA NO CREAR OTRO Y GRABARLO SOBRE EL MISMO QUE LLEGA, PERO LO HAGO ASÍ

                        userEntityToBeSaved.setName(userEntity.getName() != null && !userEntity.getName().isEmpty() ? userEntity.getName() : currentName);
                        userEntityToBeSaved.setSurname(userEntity.getSurname() != null && !userEntity.getSurname().isEmpty() ? userEntity.getSurname() : currentSurname);
                        userEntityToBeSaved.setPhone(userEntity.getPhone() != null && !userEntity.getPhone().isEmpty() ? userEntity.getPhone() : currentPhone);
                        userEntityToBeSaved.setAllergens(userEntity.getAllergens() != null && !userEntity.getAllergens().isEmpty() ? userEntity.getAllergens() : currentAllergens);
                        // LOS VALORES PARA EMAIL, CITY Y ADDRESS SE MANTIENEN LOS QUE HABÍA GUARDADOS
                        userEntityToBeSaved.setEmail(currentEmail);
                        userEntityToBeSaved.setCity(currentCity);
                        userEntityToBeSaved.setAddress(currentAddress);

                        // AHORA GUARDAMOS EL OBJETO ENTERO RECIÉN CONFIGURADO Y LLENADO EN REALTIME
                        userRef.setValue(userEntityToBeSaved, ((databaseError, databaseReference) -> {
                            if(databaseError == null){                                            // SI NO HAY ERROR
                                userRef.addListenerForSingleValueEvent(new ValueEventListener() { // VUELVO A ENTRAR EN EL NODO PARA VER SI SE HA GRABADO
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()){
                                            UserEntity updatedEntity = new UserEntity();
                                            updatedEntity.setName(dataSnapshot.child("name").getValue(String.class));
                                            updatedEntity.setSurname(dataSnapshot.child("surname").getValue(String.class));
                                            updatedEntity.setEmail(dataSnapshot.child("email").getValue(String.class));
                                            updatedEntity.setAddress(dataSnapshot.child("address").getValue(String.class));
                                            updatedEntity.setCity(dataSnapshot.child("city").getValue(String.class));
                                            updatedEntity.setPhone(dataSnapshot.child("phone").getValue(String.class));
                                            ArrayList<String> allergens = new ArrayList<>();
                                            if(dataSnapshot.child("allergens").exists()){
                                                DataSnapshot allergensSnapshot = dataSnapshot.child("allergens");
                                                for(DataSnapshot allergen : allergensSnapshot.getChildren()){
                                                    allergens.add(allergen.getValue(String.class));
                                                }
                                                updatedEntity.setAllergens(allergens);
                                            }

                                            if(!updatedEntity.getName().isEmpty() && !updatedEntity.getSurname().isEmpty() && !updatedEntity.getEmail().isEmpty()){ // SI HAY DATOS GRABADOS DEVUELVE TRUE
                                                callback.onSuccess(true);
                                            }
                                            else{
                                                callback.onSuccess(false);
                                            }
                                        }

                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        callback.onFailure(databaseError.toException());
                                    }
                                });
                            }
                            else {
                                callback.onFailure(databaseError.toException());
                            }
                        }));
                    }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                    callback.onFailure(new Exception("Error al conectarse a la base de datos"));
            }
        });
    }

    // ----------------------------------------------------------------------------------------------------------------

    public interface GetUpdateConfirmationCallback {
        void onSuccess(Boolean confirmation);
        void onFailure(Exception exception);
    }

    // ----------------------------------------------------------------------------------------------------------------

    // DICE SI EL USUARIO EXISTE EN EL NODO "users" POR LO QUE DE EXISTIR SERÍA UN "user"
    public CompletableFuture<Boolean> existsByUid(String uid) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        DatabaseReference userRef = firebaseDatabase.getReference("users").child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                future.complete(snapshot.exists());
            }
            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });

        return future;
    }

    // ----------------------------------------------------------------------------------------------------------------

    public void find(String uid, FindUserCallback callback){
        DatabaseReference userRef = databaseReference.child("users").child(uid);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        UserFindEntity userEntity = new UserFindEntity();

                        userEntity.setEmail(dataSnapshot.child("email").getValue(String.class));
                        userEntity.setName(dataSnapshot.child("name").getValue(String.class));
                        userEntity.setSurname(dataSnapshot.child("surname").getValue(String.class));
                        userEntity.setAddress(dataSnapshot.child("address").getValue(String.class));
                        userEntity.setCity(dataSnapshot.child("city").getValue(String.class));
                        userEntity.setPhone(dataSnapshot.child("phone").getValue(String.class));

                        ArrayList<String> allergens = new ArrayList<>();
                        if(dataSnapshot.child("allergens").exists()){
                            DataSnapshot allergensSnapshot = dataSnapshot.child("allergens");
                            for(DataSnapshot allergen : allergensSnapshot.getChildren()){
                                allergens.add(allergen.getValue(String.class));
                            }
                            userEntity.setAllergens(allergens);
                        }

                        if(!userEntity.getName().isEmpty() && !userEntity.getEmail().isEmpty()){
                            callback.onSuccess(userEntity);
                        }
                        else{
                            callback.onFailure(new Exception("El usuario tiene los campos vacíos en base de datos"));
                        }
                    }
                    else{
                        callback.onFailure(new Exception("El usuario no se ha encontrado"));
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) { // FALTA MANEJAR EL ERROR
                    callback.onFailure(new Exception("Error de conexión a base de datos"));
                }
            });
    }

    // ----------------------------------------------------------------------------------------------------------------

    public interface FindUserCallback{
        void onSuccess(UserFindEntity userFindEntity);
        void onFailure(Exception exception);
    }
    // ----------------------------------------------------------------------------------------------------------------

}
