package com.example.covidinfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.covidinfo.Database.AppDatabase;
import com.example.covidinfo.Database.Country;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

public class CountryDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_details);

        Bundle datos = getIntent().getExtras();
        if (datos != null)
        {
            String countryName = datos.getString("country");
            TextView pais = (TextView)findViewById(R.id.pais);
            pais.setText(countryName);
        }

        this.searchFavInfo();
    }

    public void toggleFav(View view) {
        TextView pais = (TextView)findViewById(R.id.pais);
        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database-name").fallbackToDestructiveMigration().allowMainThreadQueries().build();
        Country country=db.countryDao().findByName((String) pais.getText());
        ImageButton favIcon = findViewById(R.id.favIcon);
        if(country==null) {
            Country c1 = new Country((String) pais.getText());
//        TextView totalActivos = (TextView) findViewById(R.id.totalActivos);
//        TextView totalConfirmados = (TextView) findViewById(R.id.totalConfirmados);
//        TextView totalMuertes = (TextView) findViewById(R.id.totalMuertes);
//        TextView nuevosConfirmados = (TextView) findViewById(R.id.nuevosConfirmados);
//        TextView nuevosMuertes = (TextView) findViewById(R.id.nuevosMuertes);
//        TextView fecha = (TextView) findViewById(R.id.fecha);
            db.countryDao().insert(c1);
            Drawable d = getResources().getDrawable(R.drawable.si);
            favIcon.setImageDrawable(d);
            Toast.makeText(getApplicationContext(), "Agregado a favoritos", Toast.LENGTH_SHORT).show();
        } else {
            db.countryDao().delete(country);
            Drawable d = getResources().getDrawable(R.drawable.no);
            favIcon.setImageDrawable(d);
            Toast.makeText(getApplicationContext(), "Eliminado de favoritos", Toast.LENGTH_SHORT).show();
        }
        db.close();
//        Toast.makeText(getApplicationContext(), "Hacer el toggleFav", Toast.LENGTH_SHORT).show();
    }

    public void volver(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void searchFavInfo() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://api.covid19api.com/total/dayone/country/";
        // totalActivos, totalConfirmados, totalMuertes, nuevosConfirmados, nuevosMuertes
        final TextView totalActivos = (TextView) findViewById(R.id.totalActivos);
        final TextView totalConfirmados = (TextView) findViewById(R.id.totalConfirmados);
        final TextView totalMuertes = (TextView) findViewById(R.id.totalMuertes);
        final TextView nuevosConfirmados = (TextView) findViewById(R.id.nuevosConfirmados);
        final TextView nuevosMuertes = (TextView) findViewById(R.id.nuevosMuertes);
        final TextView fecha = (TextView) findViewById(R.id.fecha);
        String country;
        country = (String) ((TextView) findViewById(R.id.pais)).getText();

        //----------------------
        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database-name").allowMainThreadQueries().build();
        Country pais=db.countryDao().findByName(country);
        ImageButton favIcon = findViewById(R.id.favIcon);
        if(pais!=null) {
            Drawable d = getResources().getDrawable(R.drawable.si);
            favIcon.setImageDrawable(d);
        } else {
            Drawable d = getResources().getDrawable(R.drawable.no);
            favIcon.setImageDrawable(d);
        }
        db.close();
        //------------------------

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url+country,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray jsonResponse;
                        try {
                            jsonResponse = new JSONArray(response);
                            if (jsonResponse.length() > 0){
                                totalActivos.setText("aaa");
                                totalConfirmados.setText("bbb");
                                totalMuertes.setText("ccc");
                                nuevosConfirmados.setText("123");
                                nuevosMuertes.setText("456");
                                fecha.setText("hoy?");
//                            titleView.setText(jsonResponse.getJSONObject(0).getString("Country"));
//CONTROLAR QUE TENGA ALGUN ELEMENTO -> Antarctica no devuelve nada!  getJSONObject(0) -> no tiene posicion 0
                                fecha.setText(jsonResponse.getJSONObject(0).getString("Date"));
                            } //else {}

                        } catch (JSONException e) {
//                            titleView.setText(c.name);
                            // Cargar desde la db
                            Toast.makeText(getApplicationContext(), "Error3", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Cargar desde la db
//                titleView.setText(c.name);
                Toast.makeText(getApplicationContext(), "Error4", Toast.LENGTH_SHORT).show();
            }
        });
//        Toast.makeText(getApplicationContext(), "https://api.covid19api.com/total/dayone/country/"+country, Toast.LENGTH_SHORT).show();
        queue.add(stringRequest);
        // Add the request to the RequestQueue.

    }
}