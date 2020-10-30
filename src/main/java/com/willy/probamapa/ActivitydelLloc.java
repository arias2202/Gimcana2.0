package com.willy.probamapa;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ActivitydelLloc extends AppCompatActivity implements View.OnClickListener {

    Button scanBtn;
    ImageView imatge_pre;
    TextView markertxt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activitydel_lloc);




        imatge_pre=findViewById(R.id.imageView_lloc);
        markertxt=findViewById(R.id.txt_titol_vid);
        Intent intent = getIntent();
        String title=intent.getStringExtra("title");
        final Pregunta pregunta = (Pregunta) intent.getSerializableExtra("pregunta");
        markertxt.setText(title);
        LoadImage loadImage = new LoadImage(imatge_pre);
        loadImage.execute(pregunta.url_imatge_pre);


        scanBtn = findViewById(R.id.scanBtn);
        scanBtn.setOnClickListener(this);

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


    @Override
    public void onClick(View v) {
      scanCode();
    }

    private void scanCode(){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(Scanner.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Escanejant codi");
        integrator.initiateScan();
    }

protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    Intent intent = getIntent();
    final Pregunta pregunta = (Pregunta) intent.getSerializableExtra("pregunta");
    IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
    if (result != null) {
        if (result.getContents() != null) {


            if(result.getContents().toString().equals(pregunta.code)){


                        if ((pregunta.type).equals("Text")){
                            Intent intent2 = new Intent(ActivitydelLloc.this, ActivityPregunta.class);
                            intent2.putExtra("pregunta", pregunta);
                            finish();
                            ActivitydelLloc.this.startActivity(intent2);
                        }
                        else if ((pregunta.type).equals("Video")){
                            Intent intent2 = new Intent(ActivitydelLloc.this, ActivityVideo.class);
                            intent2.putExtra("pregunta", pregunta);
                            finish();
                            ActivitydelLloc.this.startActivity(intent2);
                        }

                        else if ((pregunta.type).equals("Foto")){
                            Intent intent2 = new Intent(ActivitydelLloc.this, ActivityPhoto.class);
                            intent2.putExtra("pregunta", pregunta);
                            finish();
                            ActivitydelLloc.this.startActivity(intent2);
                        }




            }
            else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Aquest no és el codi de la proba "+pregunta.nom_lloc);
            builder.setTitle("Codi incorrecte");
            builder.setPositiveButton("Torna a escanejar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    scanCode();
                }
            }).setNegativeButton("Para", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();}
        } else {
            Toast.makeText(this, "No hi ha Resultats", Toast.LENGTH_LONG).show();
        }
    } else {
        super.onActivityResult(requestCode, resultCode, data);
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
           imatge_pre.setImageBitmap(bitmap);
        }
    }
}