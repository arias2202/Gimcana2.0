package com.willy.probamapa;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActivityPregunta extends AppCompatActivity {
    String url ="https://elcentrepoblenou.cat/gimcana/register.php";
    String proves,proves_updated;
    Button EnviaBtn;
    TextView TitolTxt,PreguntaTxt;
    EditText RespostaEt;
    int userId,preguntaId;
    SharedPreferences mPrefs;
    String username;
    SharedPreferences.Editor mEditor;
    Pregunta pregunta;
    ArrayList<Pregunta> preguntaList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pregunta);
        Intent intent = getIntent();

        mPrefs = getSharedPreferences("label", 0);
        mEditor = mPrefs.edit();
       pregunta = (Pregunta) intent.getSerializableExtra("pregunta");
        mPrefs.getString("username", "");
        userId = mPrefs.getInt("id", 0);
        preguntaId = pregunta.id;
        TitolTxt=findViewById(R.id.txt_titol_vid);
        TitolTxt.setText(pregunta.nom_lloc+  "\nPregunta:");
        RespostaEt = findViewById(R.id.EtResposta);
        PreguntaTxt = findViewById(R.id.txt_pregunta);
        PreguntaTxt.setText(pregunta.question);

        proves = mPrefs.getString("proves", "default_value_if_variable_not_found");
        StringBuilder proves_nou= new StringBuilder(proves);
        proves_nou.setCharAt(preguntaId-1,'S');
        proves_updated = proves_nou.toString();

        EnviaBtn=findViewById(R.id.EnviaBtn);
        EnviaBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (RespostaEt.getText().toString() != "") {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityPregunta.this);
                                                builder.setMessage("Recorda que una vegada enviada, no la podràs modificar.");
                                                builder.setTitle("Estas a punt d'enviar la resposta");
                                                builder.setPositiveButton("Envia!", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        EnviaRespostaPhP();

                                                    }
                                                }).setNegativeButton("Encara no", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                    }
                                                });
                                                AlertDialog dialog = builder.create();
                                                dialog.show();
                                            }
                                            else{
                                                Toast.makeText(ActivityPregunta.this,"Encara no has escrit res!",Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                    }
        );





    }

    private void EnviaRespostaPhP() {

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Toast.makeText(ActivityPregunta.this,response,Toast.LENGTH_SHORT).show();

                if(response.equals("Resposta penjada correctament")){

                    PreguntaCorrecta();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ActivityPregunta.this,"Hi ha hagut problemes, torna-ho a intentar",Toast.LENGTH_SHORT).show();

            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("id_user", String.valueOf(userId));
                params.put("id_question", String.valueOf(preguntaId));
                params.put("answer", RespostaEt.getText().toString());
                params.put("proves",proves_updated);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ActivityPregunta.this);
        requestQueue.add(request);


    }

    private void PreguntaCorrecta() {


        mEditor.putString("proves", proves_updated).commit();
        Intent intent = new Intent(ActivityPregunta.this, ActivitydelLlocSuperat.class);
        intent.putExtra("pregunta",pregunta);
        finish();
        ActivityPregunta.this.startActivity(intent);



    }

}
