package com.willy.probamapa;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback{
    private static  final String Preguntes_url = "https://elcentrepoblenou.cat/gimcana/preguntes.php";
    private GoogleMap mMap;
    private RecyclerView.Adapter mAdapter;
    private ImageButton logoutBtn, question_button;
    final String telefon_contacte = "0034664473392";
    ArrayList<Pregunta> preguntaList =new ArrayList<Pregunta>();
    ArrayList<LatLng>LatLngList=new ArrayList<LatLng>();
    String proves,username;
    ArrayList<String> nom_llocsList=new ArrayList<String>();
    Dialog myDialog;
    TextView countdown;
    Long time_end, time_current,timeLeft,time_win;
    String url ="https://elcentrepoblenou.cat/gimcana/wintime.php";



    private CountDownTimer countDownTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences mPrefs = getSharedPreferences("label", 0);
        username = mPrefs.getString("username" ,"");
        proves =  mPrefs.getString("proves" ,"NNNNNNNNNNNNNNNNNNNNNNNN");
        time_end =mPrefs.getLong("date_fin", 0)*1000;
        time_win =mPrefs.getLong("time_win", 0);
        time_current = System.currentTimeMillis();
        timeLeft = time_end-time_current;
        if(time_win!=0){
            timeLeft = time_win;
        }
        GetPreguntes();
        myDialog=new Dialog(this);



        super.onCreate(savedInstanceState);







        setContentView(R.layout.activity_maps);






        logoutBtn = findViewById(R.id.bLogout);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setMessage("Estas segur de que vols tancar sessió?");
                builder.setTitle("Vigila!");
                builder.setPositiveButton("Sortir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Loggout();
                    }
                }).setNegativeButton("Encara no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });





        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        question_button = findViewById(R.id.question_btn);
        question_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               ShowPopup2();
            }
        });


        if(proves.equals("SSSSSSSSSSSSSSSSSSSSSSSS")){
            ShowPopup();
            if (time_win==0){
                SharedPreferences.Editor mEditor = mPrefs.edit();
                time_win = timeLeft;
                mEditor.putLong("time_win", time_win).commit();
                EnviaTempsPhP();
            }

        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.


        countdown = findViewById(R.id.countdown);

        if (time_win != 0){
            int hours = (int) (time_win / 3600000);
            int minutes = (int) (time_win % 3600000 / 60000);
            int seconds = (int) (time_win % 60000 / 1000);

            String timeLeftText;

            timeLeftText = "" + hours;
            timeLeftText += ":";
            if (minutes < 10) timeLeftText += "0";
            timeLeftText += minutes;
            timeLeftText += ":";
            if (seconds < 10) timeLeftText += "0";
            timeLeftText += seconds;

            countdown.setText(timeLeftText);
            countdown.setTextColor(Color.GREEN);
        }
        else {
            startTimer();
        }
    }


public void startTimer(){
        countDownTimer = new CountDownTimer(timeLeft,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft=millisUntilFinished;
                updateTimer();
            }

            @Override
            public void onFinish() {
                countdown.setText("0:00:00");
                countdown.setTextColor(Color.BLACK);
                countdown.setBackgroundColor(Color.RED);
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setMessage("Si voleu podeu seguir jugant i acabar les proves que us queden, però no contaran per la puntuació final.");
                builder.setTitle("Fi del temps!");
                builder.setPositiveButton("Torna al mapa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setNegativeButton("Seguir Jugant!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

            }
        }.start();
}


    public void updateTimer(){

            if (timeLeft < 30000){
                countdown.setTextColor(Color.RED);
            }


            int hours = (int) (timeLeft / 3600000);
            int minutes = (int) (timeLeft % 3600000 / 60000);
            int seconds = (int) (timeLeft % 60000 / 1000);

            String timeLeftText;

            timeLeftText = "" + hours;
            timeLeftText += ":";
            if (minutes < 10) timeLeftText += "0";
            timeLeftText += minutes;
            timeLeftText += ":";
            if (seconds < 10) timeLeftText += "0";
            timeLeftText += seconds;

            countdown.setText(timeLeftText);





    }


    private void GetPreguntes() {


        StringRequest stringRequest = new StringRequest(Request.Method.GET, Preguntes_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        try {
                            JSONArray preguntes = new JSONArray(response);

                            for(int i=0;i<preguntes.length();i++){

                                JSONObject preguntesObject = preguntes.getJSONObject(i);
                                int id = preguntesObject.getInt("id");
                                String question =preguntesObject.getString("question");
                                String type =preguntesObject.getString("type");
                                String code =preguntesObject.getString("code");
                                Float lat = BigDecimal.valueOf(preguntesObject.getDouble("lat")).floatValue();
                                Float longi = BigDecimal.valueOf(preguntesObject.getDouble("longi")).floatValue();
                                String nom_lloc =preguntesObject.getString("nom_lloc");
                                String url_imatge_pre =preguntesObject.getString("url_imatge_pre");
                                String url_imatge_post =preguntesObject.getString("url_imatge_post");
                                Character ch = proves.charAt(i);
                                boolean superat = false;
                                if (ch.equals('S')){
                                    superat=true;
                                }

                                Pregunta pregunta =new Pregunta(id,question,type,code,lat,longi,nom_lloc,url_imatge_pre,url_imatge_post,superat);
                                preguntaList.add(pregunta);
                                LatLngList.add(new LatLng(pregunta.lat,pregunta.longi));
                                nom_llocsList.add(pregunta.nom_lloc);
                                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                        .findFragmentById(R.id.map);
                                mapFragment.getMapAsync(MapsActivity.this);
                            }

                        } catch (Exception e) {



                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MapsActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();

            }

        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);


    }

    private void Loggout() {

        SharedPreferences mPrefs = getSharedPreferences("label", 0);
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.putString("username", "").commit();
        mEditor.putString("proves", "").commit();
        mEditor.putLong("date_fin", 0).commit();
        mEditor.putLong("time_win", 0).commit();
        mEditor.putBoolean("logged", false).commit();
        mEditor.putInt("id",0).commit();
        Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
        finish();
        MapsActivity.this.startActivity(intent);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {


        mMap = googleMap;

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.mapstyle));

            if (!success) {
                Log.e("MapActivity", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapActivity", "Can't find style. Error: ", e);
        }


        // Add a marker in Sydney and move the camera
        for (int i=0;i<LatLngList.size();i++) {

                Marker marker=mMap.addMarker(new MarkerOptions().position(LatLngList.get(i)).title(String.valueOf(nom_llocsList.get(i))));
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.virus));
                if(preguntaList.get(i).superat){
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.coronavirus));
                }
            }
        LatLng centrepb9 = new LatLng(41.399242,2.203749);
        CameraPosition cameraPosition = new CameraPosition.Builder()
            .target(centrepb9)
                .zoom(13)
                .tilt(30)
                .bearing(-45)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(centrepb9,15));


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {


                String markertitle=marker.getTitle();
                Pregunta pregunta_marker = Pregunta_pel_lloc(preguntaList,markertitle);

                if(!pregunta_marker.superat) {
                    Intent intent = new Intent(MapsActivity.this, ActivitydelLloc.class);
                    intent.putExtra("title", markertitle);
                    intent.putExtra("pregunta", pregunta_marker);
                    startActivity(intent);
                }
                else{

                    Intent intent = new Intent(MapsActivity.this, ActivitydelLlocSuperat.class);
                    intent.putExtra("pregunta", pregunta_marker);
                    startActivity(intent);



                }
                return false;
            }
        });





}

    public void ShowPopup(){
        MediaPlayer cheers = MediaPlayer.create(MapsActivity.this,R.raw.cheer);
        TextView txtclose;
        myDialog.setContentView(R.layout.custompopup);
        cheers.start();
        txtclose =(TextView) myDialog.findViewById(R.id.x_button);
        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
        myDialog.show();
    }



    public void ShowPopup2(){

        TextView txtclose;
        myDialog.setContentView(R.layout.creditspopup);
        txtclose =(TextView) myDialog.findViewById(R.id.x_button);
        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
        ImageButton whatsapp_btn;
        whatsapp_btn = (ImageButton) myDialog.findViewById(R.id.whats_button);
        whatsapp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("smsto:"+telefon_contacte);
                Intent i = new Intent(Intent.ACTION_SENDTO,uri);
                i.setPackage("com.whatsapp");
                startActivity(i);
            }
        });

        ImageButton phone_btn;
        phone_btn = (ImageButton) myDialog.findViewById(R.id.phone_button);
        phone_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("tel:"+telefon_contacte);
                Intent i = new Intent(Intent.ACTION_DIAL,uri);

                startActivity(i);
            }
        });




        myDialog.show();
    }




    public Pregunta Pregunta_pel_lloc(List<Pregunta> llistapreguntes, String nom_lloc)
    {
        for(int i=0;i<llistapreguntes.size();i++) {
            if(llistapreguntes.get(i).nom_lloc.equals(nom_lloc)){
                return llistapreguntes.get(i);
            }

        }

        return null;
    }


    public void onBackPressed() {
        moveTaskToBack(false);
    }


    private void EnviaTempsPhP() {

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Toast.makeText(MapsActivity.this,response,Toast.LENGTH_SHORT).show();

                if(response.equals("Resposta penjada correctament")){

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MapsActivity.this,"Hi ha hagut problemes, torna-ho a intentar",Toast.LENGTH_SHORT).show();

            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                SharedPreferences mPrefs = getSharedPreferences("label", 0);
                int userId = mPrefs.getInt("id", 0);
                params.put("id_user", String.valueOf(userId));
                params.put("temps", String.valueOf(time_win));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(MapsActivity.this);
        requestQueue.add(request);


    }


}


