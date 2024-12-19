package com.example.homiyummy.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.springframework.stereotype.Repository;
import java.util.concurrent.CompletableFuture;


@Repository
public class AuthRepository {

    private final FirebaseAuth firebaseAuth;

    public AuthRepository( FirebaseAuth firebaseAuth){
        this.firebaseAuth = firebaseAuth;
    }

    // EXTRAE EL UID DEL USUARIO ASOCIADO AL EMAIL APORTADO
    public CompletableFuture<String> getUidFromEmail(String email) {
        // CREAMOS UN FUTURO DONDE GUARDAR EL EMAIL
        CompletableFuture<String> futureId = new CompletableFuture<>();
        try {
            // USAMOS UN OBJETO UserRecord DONDE GUARDAR EL USUARIO
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
            // COMPLETAMOS EL FUTURO PASÁNDOLE EL UID DEL USUARIO
            futureId.complete(userRecord.getUid());
        } catch (FirebaseAuthException e) {
            // SI SALE MAL
            futureId.completeExceptionally(e);
        }
        // DEVOLVEMOS EL FUTURO AL SERVICE
        return futureId;
    }
}
