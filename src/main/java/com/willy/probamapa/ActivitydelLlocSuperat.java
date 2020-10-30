package com.willy.probamapa;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;

public class ActivitydelLlocSuperat extends AppCompatActivity {

    private static  final String Preguntes_url = "https://elcentrepoblenou.cat/gimcana/preguntes.php";
    ImageView imatge_post;
    Button tornaBtn;
    TextView markertxt;
    ArrayList<Pregunta> preguntaList  = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitydel_llocsuperat);




        imatge_post=findViewById(R.id.imageView_lloc);
        markertxt=findViewById(R.id.txt_titol_vid);
        Intent intent = getIntent();

        final Pregunta pregunta = (Pregunta) intent.getSerializableExtra("pregunta");
        String title=pregunta.nom_lloc;
        markertxt.setText(title);
        LoadImage loadImage = new LoadImage(imatge_post);
        loadImage.execute(pregunta.url_imatge_post);



        MediaPlayer cheers = MediaPlayer.create(ActivitydelLlocSuperat.this,R.raw.successound);
        cheers.start();

        tornaBtn = findViewById(R.id.tornarbtn);
        tornaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               LaunchMap();

            }
        });

    }


    public void LaunchMap(){
        Intent intent = new Intent(ActivitydelLlocSuperat.this, MapsActivity.class);
        finish();
        ActivitydelLlocSuperat.this.startActivity(intent);

    }




    public static Drawable LoadImageFromWebOperations(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        } catch (Exception e) {
            return null;
        }
    }






    private class LoadImage extends AsyncTask<String,Void,Bitmap> {
        ImageView imageView;
        public  LoadImage(ImageView imatge_pre){
            this.imageView = imatge_pre;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            String urlLink = strings[0];
            Bitmap bitmap =null;
            try {
                InputStream inputStream = new URL(urlLink).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
           imatge_post.setImageBitmap(bitmap);
        }
    }

    public void onBackPressed() {
        LaunchMap();
    }
}