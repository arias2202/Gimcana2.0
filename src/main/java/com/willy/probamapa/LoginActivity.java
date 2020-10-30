package com.willy.probamapa;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LoginActivity extends AppCompatActivity {




    @Override
    protected  void onCreate(Bundle savedInstaceState) {

        super.onCreate(savedInstaceState);
        final SharedPreferences mPrefs = getSharedPreferences("label", 0);
        Boolean logged = mPrefs.getBoolean("logged", false);

        if (logged) {
            String username = mPrefs.getString("username", "default_value_if_variable_not_found");
            String proves = mPrefs.getString("proves", "default_value_if_variable_not_found");

            Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("proves", proves);
            finish();
            LoginActivity.this.startActivity(intent);

        } else {


            setContentView(R.layout.activity_login);

            final Button bLogin = (Button) findViewById(R.id.bLogin);
            final EditText etUsername = (EditText) findViewById(R.id.etUserName);
            final EditText etPassword = (EditText) findViewById(R.id.etPassword);


            bLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String username = etUsername.getText().toString();
                    final String password = etPassword.getText().toString();

                    Response.Listener<String> responseListener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                boolean success = jsonResponse.getBoolean("success");
                                if (success) {
                                    String proves = jsonResponse.getString("proves");
                                    Integer id = jsonResponse.getInt("id");
                                    Long time_end = jsonResponse.getLong("date_fin");
                                    Long time_win = jsonResponse.getLong("temps_win");


                                    if (proves.equals("null")) {
                                        proves = "NNNNNNNNNNNNNNNNNNNNNNNNN";
                                    }
                                    SharedPreferences.Editor mEditor = mPrefs.edit();
                                    mEditor.putString("username", username).commit();
                                    mEditor.putString("proves", proves).commit();
                                    mEditor.putInt("id",id).commit();
                                    mEditor.putBoolean("logged", true).commit();
                                    mEditor.putLong("date_fin", time_end).commit();
                                    mEditor.putLong("temps_win", time_win).commit();
                                    Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                                    intent.putExtra("username", username);
                                    intent.putExtra("proves", proves);
                                    intent.putExtra("timestamp", time_end);



                                    finish();
                                    LoginActivity.this.startActivity(intent);


                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                    builder.setMessage("Login Failed")
                                            .setNegativeButton("Retry", null)
                                            .create()
                                            .show();
                                }

                            } catch (JSONException  e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    LoginRequest loginRequest = new LoginRequest(username, password, responseListener);
                    RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
                    queue.add(loginRequest);


                }
            });
        }


    }



}
