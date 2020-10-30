package com.willy.probamapa;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ActivityPhoto extends AppCompatActivity {
    String url ="https://elcentrepoblenou.cat/gimcana/register.php";
    Button EnviaBtn, RecordBtn, GaleryBtn;
    private TextView TitolTxt, PreguntaTxt;
    private int ACTIVITY_START_CAMERA_APP = 101;
    private ImageView imageView;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int GALLERY_REQUEST_CODE = 102;
    private Uri image;
    String file_path = null;
    int userId,preguntaId;
    ProgressBar progressBar;
    SharedPreferences mPrefs;
    SharedPreferences.Editor mEditor;
    public String username,nomLloc;
    Pregunta pregunta;
    String proves,proves_updated;
    private ContentValues values;
    private Bitmap thumbnail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        pregunta = (Pregunta) intent.getSerializableExtra("pregunta");
        mPrefs = getSharedPreferences("label", 0);
        mEditor = mPrefs.edit();
        username = mPrefs.getString("username", "");
        userId = mPrefs.getInt("id", 0);
        preguntaId = pregunta.id;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        proves = mPrefs.getString("proves", "default_value_if_variable_not_found");
        StringBuilder proves_nou= new StringBuilder(proves);
        proves_nou.setCharAt(preguntaId-1,'S');
        proves_updated = proves_nou.toString();



        nomLloc = pregunta.nom_lloc.replaceAll("[^a-zA-Z0-9]", " ");

        TitolTxt = findViewById(R.id.txt_titol_vid);
        TitolTxt.setText(pregunta.nom_lloc + "\n" + pregunta.type + ":");

        PreguntaTxt = findViewById(R.id.txt_pregunta);
        PreguntaTxt.setText(pregunta.question);

        EnviaBtn = findViewById(R.id.EnviaBtn);
        RecordBtn = findViewById(R.id.grababt);
        GaleryBtn = findViewById(R.id.gallerybt);
        imageView = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progress);
        RecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Build.VERSION.SDK_INT>=23){

                    if(checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){

                            values = new ContentValues();
                            values.put(MediaStore.Images.Media.TITLE, "New Picture");
                            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                            image = getContentResolver().insert(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, image);
                            startActivityForResult(intent, ACTIVITY_START_CAMERA_APP);





                    }


                    else {
                        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    }
                }
                else{

                    values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "New Picture");
                    values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                    image = getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, image);
                    startActivityForResult(intent, ACTIVITY_START_CAMERA_APP);

                }


            }
        });



        GaleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(Build.VERSION.SDK_INT>=23){

                    if(checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                        Intent intent1 = new Intent();
                        intent1.setType("image/*");
                        intent1.setAction(Intent.ACTION_PICK);
                        startActivityForResult(intent1,GALLERY_REQUEST_CODE);

                    }
                    else{
                        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    }
                }
                else{
                    Intent intent1 = new Intent();
                    intent1.setType("image/*");
                    intent1.setAction(Intent.ACTION_PICK);
                    startActivityForResult(intent1,GALLERY_REQUEST_CODE);
                }




            }
        });


        EnviaBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {


                                            if (file_path != null) {


                                                        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityPhoto.this);
                                                        builder.setMessage("Recorda que una vegada enviada, no la podràs modificar.");
                                                        builder.setTitle("Estas a punt d'enviar la resposta");
                                                        builder.setPositiveButton("Envia!", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                uploadFile();
                                                            }
                                                        }).setNegativeButton("Encara no", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {

                                                            }
                                                        });
                                                AlertDialog dialog = builder.create();
                                                dialog.show();


                                            } else {
                                                Toast.makeText(ActivityPhoto.this, "No heu seleccionat cap foto", Toast.LENGTH_SHORT).show();
                                            }

                                        }});
    }



    private void requestPermission(String permission) {
        if(ActivityCompat.shouldShowRequestPermissionRationale(ActivityPhoto.this, permission)){
            Toast.makeText(ActivityPhoto.this, "Siusplau, es necessiten els permissos",Toast.LENGTH_SHORT).show();

        }
        else{
            ActivityCompat.requestPermissions(ActivityPhoto.this,new String[]{permission},PERMISSION_REQUEST_CODE);

        }
    }


    private boolean checkPermission(String permission) {
        int result = ContextCompat.checkSelfPermission(ActivityPhoto.this,permission);
        if(result== PackageManager.PERMISSION_GRANTED){
            return true;
        }
        else{
            return false;
        }



    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_START_CAMERA_APP && resultCode == RESULT_OK  ) {


            try {
                thumbnail = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), image);
                imageView.setImageBitmap(thumbnail);
                file_path = getRealPathFromURI(image);
            } catch (Exception e) {
                e.printStackTrace();
            }



        }

        else if((requestCode == GALLERY_REQUEST_CODE) && resultCode == RESULT_OK  ){



            image = data.getData();
            String filePath=getRealPathFromUri(data.getData(), ActivityPhoto.this);
            this.file_path=filePath;

            imageView.setImageURI(image);
        }

    }
    public String getRealPathFromUri(Uri uri, Activity activity){
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor=activity.getContentResolver().query(uri,proj,null,null,null);
        if(cursor==null){
            return uri.getPath();
        }
        else{
            cursor.moveToFirst();
            int id=cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(id);
        }
    }


    private void uploadFile() {
        UploadTask uploadTask=new UploadTask();
        uploadTask.execute(new String[]{file_path});


    }

    public class UploadTask extends AsyncTask<String,String,String>{

        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            progressBar.setVisibility(View.GONE);
            if(s.equalsIgnoreCase("true")){
                Toast.makeText(ActivityPhoto.this,"Arxiu penjant-se", Toast.LENGTH_SHORT).show();
                EnviaRespostaPhP();
            }
            else{
                Toast.makeText(ActivityPhoto.this, "Problemes penjant l'arxiu",Toast.LENGTH_SHORT).show();
            }
        }
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }
        @Override
        protected String doInBackground(String... strings) {
            if(uploadFile(strings[0])){
            return "true";
            }else
            {
            return  "failed";
            }

        }
        private boolean uploadFile(String path){
            File file=new File(path);

            try{

                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("files",nomLloc+".jpeg",RequestBody.create(MediaType.parse("*/*"),file))
                        .addFormDataPart("some_key","some_value")
                        .addFormDataPart("submit","submit")
                        .addFormDataPart("folder", username)
                        .build();

                Request request = new Request.Builder()
                        .url("https://elcentrepoblenou.cat/gimcana/uploadedFiles/upload.php")
                        .post(requestBody)
                        .build();
                OkHttpClient client=new OkHttpClient();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
int i=1;
                    }
                });
                return true;
            }
            catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }
    }
    private void EnviaRespostaPhP() {

        StringRequest request = new StringRequest(com.android.volley.Request.Method.POST, url, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Toast.makeText(ActivityPhoto.this,response,Toast.LENGTH_SHORT).show();

                if(response.equals("Resposta penjada correctament")){

                    PreguntaCorrecta();
                }

            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ActivityPhoto.this,"Hi ha hagut problemes, torna-ho a intentar",Toast.LENGTH_SHORT).show();

            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("id_user", String.valueOf(userId));
                params.put("id_question", String.valueOf(preguntaId));
                params.put("answer", "https://elcentrepoblenou.cat/gimcana/uploadedFiles/"+username+"/"+nomLloc+".jpeg");
                params.put("proves",proves_updated);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ActivityPhoto.this);
        requestQueue.add(request);


    }

    private void PreguntaCorrecta() {


        mEditor.putString("proves", proves_updated).commit();
        Intent intent = new Intent(ActivityPhoto.this, ActivitydelLlocSuperat.class);
        intent.putExtra("pregunta",pregunta);
        finish();
        ActivityPhoto.this.startActivity(intent);


    }




}
