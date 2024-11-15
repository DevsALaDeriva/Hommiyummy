package com.example.homiyummy.repository;

import com.example.homiyummy.model.menu.MenuEntity;
import com.example.homiyummy.model.menu.MenuResponse;
import com.example.homiyummy.model.restaurant.RestaurantResponse;
import com.google.firebase.database.*;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class MenuRepository {

    private final FirebaseDatabase firebaseDatabase;

    public MenuRepository(FirebaseDatabase firebaseDatabase) {
        this.firebaseDatabase = firebaseDatabase;
    }
    
    public void saveMenu(MenuEntity menuEntity, getSaveMenuCallback callback) {
        
        Map<String, Object> menuEntityToSave = new HashMap<>();
        menuEntityToSave.put("date", menuEntity.getDate());
        menuEntityToSave.put("firsCourse", menuEntity.getFirstCourse());
        menuEntityToSave.put("secondCourse", menuEntity.getSecondCourse());
        menuEntityToSave.put("dessert", menuEntity.getDessert());
        menuEntityToSave.put("priceWithDessert", menuEntity.getPriceWithDessert());
        menuEntityToSave.put("priceWithNoDessert", menuEntity.getPriceNoDessert());

        DatabaseReference menuRef = firebaseDatabase.getReference("menus").child(menuEntity.getUid());
        
        menuRef.setValue(menuEntity, ((databaseError, databaseReference) ->{
          if(databaseError == null) {
              menuRef.addListenerForSingleValueEvent(new ValueEventListener() {
                  @Override
                  public void onDataChange(DataSnapshot dataSnapshot) {
                      MenuResponse menuResponse = dataSnapshot.getValue((MenuResponse.class));
                      menuResponse.setUid(menuRef.getKey());
                      callback.onMenuGot(menuResponse);
                  }

                  @Override
                  public void onCancelled(DatabaseError databaseError) {
                      callback.onFailure(databaseError.toException());
                  }
              });
              
          } else {
              callback.onFailure(databaseError.toException());
          }
            
            
        }));
        
    }
    
    public interface getSaveMenuCallback {
        void onMenuGot(MenuResponse menuResponse);
        void onFailure(Exception exception);
    }
}
