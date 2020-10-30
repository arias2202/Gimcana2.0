package com.willy.probamapa;

public class User {
    String username,password, proves;

    public User(String username, String password, String proves) {
        this.username = username;
        this.password = password;
        this.proves = proves;
    }
      public User (String username, String password){
            this.username = username;
            this.password = password;
            this.proves = "NNNNNNNNNNNNNNNNNNNNNNNN";

    }
}
