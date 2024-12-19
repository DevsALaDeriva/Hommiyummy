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


    /**
     *
     * @param userEntity USUARIO ENTRANTE (UserEntity) PARA SER GRABADO
     *                   - LO TRANSFORMAMOS EN OTRO USUARIO DEL MISMO TIPO, ESTABLECIENDO EL PASSWORD A NULL PARA QUE NO SE GUARDE EN REALTIME Y LO GUARDAMOS
     *                   - VOLVEMOS A RECUPERARLO (PARA ASEGURAR QUE SE HA GUARDADO)
     * @param callback   Y DEVOLVEMOS ESTE OBJETO UserEntity RECUPERADO Y RECIÉN OBTENIDO
     */
    public void saveUser(UserEntity userEntity, SaveUserCallback callback)  {

        // REFERENCIA DEL USUARIO EN BBDD
        DatabaseReference userRef = firebaseDatabase.getReference("users").child(userEntity.getUid());

        // ESTABLECEMOS LA PASSWORD A NULL PARA QUE NO LA GUARDE
        userEntity.setPassword(null);

        // GUARDAMOS EL USUARIO
        userRef.setValue(userEntity, (databaseError, databaseReference) -> {
            // SI NO HAY ERROR ENTRA
            if (databaseError == null) {
                // ACCEDEMOS AL LUGAR DONDE SE HA GUARDADO EL USUARIO
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // CREAMOS UN OBJETO PARA GUARDAR EL USUARIO
                        UserEntity recordedEntity = new UserEntity();
                        // OBTENEMOS TODAS LOS VALORES RECIÉN GUARDADOS EN BASE DE DATOS Y SE LAS ASIGNAMOS AL OBJETO
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
                        // MANDAMOS AL SERVICE EL OBJETO USUARIO OBTENIDO
                        callback.onSuccess(recordedEntity);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // SI HAY ERROR MANDAMOS UNA EXCEPTION
                        callback.onFailure(databaseError.toException());
                    }
                });
            } else {
                // SI HAY ERROR MANDAMOS UNA EXCEPTION
                callback.onFailure(databaseError.toException());
            }
        });
    }


    public interface SaveUserCallback {
        void onSuccess(UserEntity userEntity);
        void onFailure(Exception exception);
    }
    /**
     * ACTUALIZA DATOS DEL USUARIO EN BASE DE DATOS.
     * PARA NO PERDER DATOS EN ALGUNAS PROPIEDADES, PRIMERO GUARDA LOS VALORES EXISTENTES (SI LOS HUBIERA),
     * CREAMOS CON ELLOS OTRO USUARIO DE TIPO UserEntity
     * Y ESTE ES EL OBJETO QUE SOBRESCRIBIMOS.
     * @param userEntity OBJETO QUE REPRESENTA AL USUARIO (ENVIADO DESDE EL SERVICIO)
     * @param callback
     *      SI TIENE ÉXITO -> DEVUELVE true
     *      SI NO, -> DEVUELVE false
     */
    public void updateUserData(UserEntity userEntity, GetUpdateConfirmationCallback callback) {

        // REFERENCIA DEL USUARIO EN BBDD
        DatabaseReference userRef = firebaseDatabase.getReference("users").child(userEntity.getUid());

        // ACCEDEMOS AL USUARIO
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // SI EXISTE, ENTRAMOS
                    if(dataSnapshot.exists()){
                        // OBTENEMOS EL VALOR DE SUS PROIEDADES
                        String currentUID = dataSnapshot.child("uid").getValue(String.class);
                        String currentName = dataSnapshot.child("name").getValue(String.class);
                        String currentSurname = dataSnapshot.child("surname").getValue(String.class);
                        String currentEmail = dataSnapshot.child("email").getValue(String.class);
                        String currentPhone = dataSnapshot.child("phone").getValue(String.class);
                        String currentAddress = dataSnapshot.child("address").getValue(String.class);
                        String currentCity = dataSnapshot.child("city").getValue(String.class);

                        // SI HAY ALÉRGENOS LOS GUARDAMOS EN UN ARRAY
                        ArrayList<String> currentAllergens = new ArrayList<>();
                        if(dataSnapshot.child("allergens").exists()){
                            DataSnapshot allergensSnapshot = dataSnapshot.child("allergens");
                            for(DataSnapshot allergen : allergensSnapshot.getChildren()){
                                currentAllergens.add(allergen.getValue(String.class));
                            }
                        }

                        // CREAMOS UN USUARIO
                        UserEntity userEntityToBeSaved = new UserEntity();

                        // LE ASIGNAMOS EL VALOR DE LA PROPIEDAD ENTRANTE, SI NO LA HAY LE ASIGNAMOS LA QUE FIGURA EN BASE DE DATOS
                        userEntityToBeSaved.setName(userEntity.getName() != null && !userEntity.getName().isEmpty() ? userEntity.getName() : currentName);
                        userEntityToBeSaved.setSurname(userEntity.getSurname() != null && !userEntity.getSurname().isEmpty() ? userEntity.getSurname() : currentSurname);
                        userEntityToBeSaved.setPhone(userEntity.getPhone() != null && !userEntity.getPhone().isEmpty() ? userEntity.getPhone() : currentPhone);
                        userEntityToBeSaved.setAllergens(userEntity.getAllergens() != null && !userEntity.getAllergens().isEmpty() ? userEntity.getAllergens() : currentAllergens);
                        userEntityToBeSaved.setEmail(currentEmail);
                        userEntityToBeSaved.setCity(currentCity);
                        userEntityToBeSaved.setAddress(currentAddress);
                        userEntityToBeSaved.setUid(currentUID);
                        userEntityToBeSaved.setPassword(null);

                        // GUARDAMOS EL USUARIO EN BASE DE DATOS
                        userRef.setValue(userEntityToBeSaved, ((databaseError, databaseReference) -> {
                            if(databaseError == null){
                                // ACCEDEMOS AL USUARIO RECIÉN GUARDADO
                                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        // SI EXISTE
                                        if(dataSnapshot.exists()){
                                            // CREAMOS UN NUEVO OBJETO PARA GUARDAR EL RECIÉN GUARDADO
                                            UserEntity updatedEntity = new UserEntity();
                                            // LE ASIGNAMOS EL VALOR DEL USUARIO EN BASE DE DATOS PARA ASEGURARNOS DE QU MANDAMOS EL CORRECTO
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

                                            // SI CONTIENE DATOS
                                            if(!updatedEntity.getName().isEmpty() && !updatedEntity.getSurname().isEmpty() && !updatedEntity.getEmail().isEmpty()){ // SI HAY DATOS GRABADOS DEVUELVE TRUE
                                                // MANDAMOS true
                                                callback.onSuccess(true);
                                            }
                                            else{
                                                // SI NO, MANDAMOS false
                                                callback.onSuccess(false);
                                            }
                                        }

                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        // MANDAMOS UNA EXCEPTION
                                        callback.onFailure(databaseError.toException());
                                    }
                                });
                            }
                            else {
                                // SI HAY UN ERROR MANDAMOS UNA EXCEPTION
                                callback.onFailure(databaseError.toException());
                            }
                        }));
                    }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // // MANDAMOS UNA EXCEPTION CON CUALQUIER ERROR DE CONEXIÓN O ACCESO
                    callback.onFailure(new Exception("Error al conectarse a la base de datos"));
            }
        });
    }



    public interface GetUpdateConfirmationCallback {
        void onSuccess(Boolean confirmation);
        void onFailure(Exception exception);
    }



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


    /**
     * OBTIENE EL USUARIO GUARDADO EN BBDD. LO GUARDA EN FORMATO UserFindEntity
     * @param uid UID DEL USUARIO BUSCADO
     * @param callback USA LA INTERFAZ FindUserCallback.
     *                 SI LO ENCUENTRA DEVUELVE EL UserFindEntity AL SERVICIO
     *                 SI NO, LE ENVÍA UNA EXCEPCIÓN
     *
     */
    public void find(String uid, FindUserCallback callback){
        // REFERENCIA DEL USUARIO EN BASE DE DATOS
        DatabaseReference userRef = databaseReference.child("users").child(uid);

        // ACCEDEMOS A LA REFERENCIA
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // SI EXISTE ENTRAMOS
                    if(dataSnapshot.exists()){
                        // CREAMOS UN OBJETO USUARIO
                        UserFindEntity userEntity = new UserFindEntity();

                        // LE LLENAMOS DE CONTENIDO
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
                        // PROBAMOS EN ALGUNAS PROPOIEDADES PARA VER SI REALMENTE CONTIENE DATOS
                        if(!userEntity.getName().isEmpty() && !userEntity.getEmail().isEmpty()){
                            // MANDAMOS EL USUARIO AL SERVICE
                            callback.onSuccess(userEntity);
                        }
                        else{
                            // SI NO CONTIENE NDATOS MANDAMOS UNA EXCEPCION
                            callback.onFailure(new Exception("El usuario tiene los campos vacíos en base de datos"));
                        }
                    }
                    else{
                        // SI NO HAY USUARIO MANDAMOS UNA EXCEPCIÓN
                        callback.onFailure(new Exception("El usuario no se ha encontrado"));
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // MANDAMOS EXCEPCIÓN POR UN ERROR DE CONEXIÓN
                    callback.onFailure(new Exception("Error de conexión a base de datos"));
                }
            });
    }


    public interface FindUserCallback{
        void onSuccess(UserFindEntity userFindEntity);
        void onFailure(Exception exception);
    }

}
