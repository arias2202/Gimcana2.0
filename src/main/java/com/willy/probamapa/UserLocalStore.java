package com.willy.probamapa;

import android.content.Context;
import android.content.SharedPreferences;

public class UserLocalStore {
    public static final String SP_NAME ="userDetails";
   SharedPreferences userLocalDatabase;

   public UserLocalStore(Context context){
       userLocalDatabase =context.getSharedPreferences(SP_NAME,0);

   }

    public void storeUserData(User user){
       SharedPreferences.Editor spEditor = userLocalDatabase.edit();
       spEditor.putString("username",user.username);
        spEditor.putString("passwor",user.password);
        spEditor.putString("proves",user.proves);
        spEditor.commit();


    }

    public User getLoggedIn(){
       String username = userLocalDatabase.getString("username","");
        String password = userLocalDatabase.getString("password","");
        String proves = userLocalDatabase.getString("proves","NNNNNNNNNNNNNNNNNNNNNNNN");

        User storedUser = new User(username,password,proves);
        return storedUser;
    }
    public void setUserLoggedIn(boolean loggedIn){
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.putBoolean("loggedIn",loggedIn);
        spEditor.commit();
    }

    public boolean getUserLoggedIn(){
       if(userLocalDatabase.getBoolean("loggedIn",false)){
           return true;
       } else{
           return false;
       }
    }
    public void  clearUserData(){
       SharedPreferences.Editor spEditor = userLocalDatabase.edit();
       spEditor.clear();
       spEditor.commit();
    }
}
