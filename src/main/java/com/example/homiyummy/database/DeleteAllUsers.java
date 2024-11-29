package com.example.homiyummy.database;

import com.google.firebase.auth.*;

import java.util.List;
import java.util.stream.StreamSupport;

public class DeleteAllUsers {


//    public static void deleteAllUsers() {
//        try {
//            ListUsersPage page = FirebaseAuth.getInstance().listUsers(null);
//
//            while (page != null) {
//                for (UserRecord user : page.getValues()) {
//                    System.out.println("Eliminando usuario: " + user.getUid());
//                }
//
//                // Obtener los UIDs de los usuarios en el lote como una lista
//                List<String> uids = StreamSupport.stream(page.getValues().spliterator(), false)
//                        .map(UserRecord::getUid)
//                        .toList();
//
//                // Eliminar los usuarios en el lote
//                DeleteUsersResult result = FirebaseAuth.getInstance().deleteUsers(uids);
//
//                System.out.println("Usuarios eliminados exitosamente: " + result.getSuccessCount());
//                System.out.println("Errores al eliminar: " + result.getFailureCount());
//
//                // Manejar errores usando el índice
//                result.getErrors().forEach(error -> {
//                    int index = error.getIndex(); // Índice del usuario con error
//                    String uid = uids.get(index); // UID correspondiente
//
//                    if (AuthErrorCode.USER_NOT_FOUND.equals(error.getReason())) {
//                        System.out.println("Usuario no encontrado: " + uid);
//                    } else {
//                        System.out.println("Error al eliminar usuario con UID " + uid + ": " + error.getReason());
//                    }
//                });
//
//                // Avanzar a la siguiente página
//                page = page.getNextPage();
//            }
//
//            System.out.println("Todos los usuarios han sido eliminados.");
//        } catch (Exception e) {
//            System.out.println("Error al eliminar usuarios: " + e.getMessage());
//        }
//    }
}
