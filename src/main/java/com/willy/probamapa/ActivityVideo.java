package com.willy.probamapa;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.File;
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

public class ActivityVideo extends AppCompatActivity {
    String url ="https://elcentrepoblenou.cat/gimcana/register.php";
    Button EnviaBtn, RecordBtn, GaleryBtn;
    private TextView TitolTxt, PreguntaTxt;
    private int ACTIVITY_START_CAMERA_APP = 0;
    private VideoView videoView;
    private static final int GALLERY_REQUEST_CODE = 123;
    private Uri video;
    String file_path = null;
    int userId,preguntaId;
    public String nomLloc;
    ProgressBar progressBar;
    SharedPreferences mPrefs;
    SharedPreferences.Editor mEditor;
    String username;
    Pregunta pregunta;
    String proves,proves_updated;
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
        setContentView(R.layout.activity_video);
        proves = mPrefs.getString("proves", "default_value_if_variable_not_found");
        StringBuilder proves_nou= new StringBuilder(proves);
        proves_nou.setCharAt(preguntaId-1,'S');
        proves_updated = proves_nou.toString();



        nomLloc = pregunta.nom_lloc.replaceAll("[^a-zA-Z0-9]", " ");

        TitolTxt = findViewById(R.id.txt_titol_vid);
        TitolTxt.setText(pregunta.nom_lloc + "\nVídeo:");

        PreguntaTxt = findViewById(R.id.txt_pregunta);
        PreguntaTxt.setText(pregunta.question);

        EnviaBtn = findViewById(R.id.EnviaBtn);
        RecordBtn = findViewById(R.id.grababt);
        GaleryBtn = findViewById(R.id.gallerybt);
        videoView = findViewById(R.id.videoView);
        progressBar = findViewById(R.id.progress);
        RecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callVideoAppIntent = new Intent();
                callVideoAppIntent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
                startActivityForResult(callVideoAppIntent, ACTIVITY_START_CAMERA_APP);
            }
        });


        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                if (videoView.isPlaying()) {
                    videoView.pause();

                    return false;
                } else {

                    videoView.start();
                    return false;
                }
            }
        });


        GaleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent();
                intent1.setType("video/*");
                intent1.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(intent1, " Tria un video"), GALLERY_REQUEST_CODE);


            }
        });


        EnviaBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            if (file_path != null) {
                                                File file = new File(file_path);
                                                int file_size = Integer.parseInt(String.valueOf(file.length()/1024));

                                                if (file_size > 63000){
                                                    Toast.makeText(ActivityVideo.this, "L'arxiu pesa massa, proveu amb un video més curt o d'una resolució més baixa", Toast.LENGTH_LONG).show();
                                                }
                                                else {


                                                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityVideo.this);
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
                                                }
                                            } else {
                                                Toast.makeText(ActivityVideo.this, "No heu seleccionat cap video", Toast.LENGTH_SHORT).show();
                                            }

                                        }});
    }



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == ACTIVITY_START_CAMERA_APP || requestCode == GALLERY_REQUEST_CODE) && resultCode == RESULT_OK  ) {
            video = data.getData();
            String filePath=getRealPathFromUri(data.getData(),ActivityVideo.this);
            videoView.setVideoURI(video);
            videoView.start();
            this.file_path=filePath;
            File file=new File(filePath);




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
                Toast.makeText(ActivityVideo.this,"File uploaded", Toast.LENGTH_SHORT).show();
                EnviaRespostaPhP();
            }
            else{
                Toast.makeText(ActivityVideo.this, "Failed upload",Toast.LENGTH_SHORT).show();
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
                        .addFormDataPart("files", nomLloc+".mp4",RequestBody.create(MediaType.parse("*/*"),file))
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
                        int i =1;
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

                Toast.makeText(ActivityVideo.this,response,Toast.LENGTH_SHORT).show();

                if(response.equals("Resposta penjada correctament")){

                    PreguntaCorrecta();
                }

            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ActivityVideo.this,"Hi ha hagut problemes, torna-ho a intentar",Toast.LENGTH_SHORT).show();

            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("id_user", String.valueOf(userId));
                params.put("id_question", String.valueOf(preguntaId));
                params.put("answer", "https://elcentrepoblenou.cat/gimcana/uploadedFiles/"+username+"/"+nomLloc+".mp4");
                params.put("proves",proves_updated);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ActivityVideo.this);
        requestQueue.add(request);


    }

    private void PreguntaCorrecta() {


        mEditor.putString("proves", proves_updated).commit();
        Intent intent = new Intent(ActivityVideo.this, ActivitydelLlocSuperat.class);
        intent.putExtra("pregunta",pregunta);
        finish();
        ActivityVideo.this.startActivity(intent);

    }


}
