package com.example.covidinfo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
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
import org.json.JSONObject;

import java.util.Date;

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
//        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database-name").fallbackToDestructiveMigration().allowMainThreadQueries().build();
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        Country country=db.countryDao().findByName((String) pais.getText());
        ImageButton favIcon = findViewById(R.id.favIcon);
        if(country==null) {
            Country c1 = new Country((String) pais.getText());
        String totalActivos = ((TextView) findViewById(R.id.totalActivos)).getText().toString();
        String totalConfirmados = ((TextView) findViewById(R.id.totalConfirmados)).getText().toString();
        String totalMuertes = ((TextView) findViewById(R.id.totalMuertes)).getText().toString();
        String nuevosConfirmados = ((TextView) findViewById(R.id.nuevosConfirmados)).getText().toString();
        String nuevosMuertes = ((TextView) findViewById(R.id.nuevosMuertes)).getText().toString();
//        Date fecha = new Date(((TextView) findViewById(R.id.fecha)).toString());
            db.countryDao().insert(c1);
//            Drawable d = getResources().getDrawable(R.drawable.si);
//            favIcon.setImageDrawable(d);
            favIcon.setImageResource(R.drawable.si);
            Toast.makeText(getApplicationContext(), "Agregado a favoritos", Toast.LENGTH_SHORT).show();
        } else {
            db.countryDao().delete(country);
//            Drawable d = getResources().getDrawable(R.drawable.no);
//            favIcon.setImageDrawable(d);
            favIcon.setImageResource(R.drawable.no);
            Toast.makeText(getApplicationContext(), "Eliminado de favoritos", Toast.LENGTH_SHORT).show();
        }
    }

    public void volver(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void searchFavInfo() {
        final RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://api.covid19api.com/summary";
        // totalActivos, totalConfirmados, totalMuertes, nuevosConfirmados, nuevosMuertes
        final String country;
        country = (String) ((TextView) findViewById(R.id.pais)).getText();

        //----------------------
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        Country pais=db.countryDao().findByName(country);
        ImageButton favIcon = findViewById(R.id.favIcon);
        if(pais!=null) {
            Drawable d = getResources().getDrawable(R.drawable.si);
            favIcon.setImageDrawable(d);
        } else {
            Drawable d = getResources().getDrawable(R.drawable.no);
            favIcon.setImageDrawable(d);
        }
        //------------------------

        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray jsonResponse;
                        try {
                            jsonResponse = (new JSONObject(response)).getJSONArray("Countries");
                            if (jsonResponse.length() > 0) {
                                int i = 0;
                                while ((i < jsonResponse.length() - 1) && !jsonResponse.getJSONObject(i).getString("Country").equals(country)) {
                                    i++;
                                }
                                // CONTROLAR SI POR MOTIVO RARO NO LO ENCUENTRA
                                JSONObject jobj = jsonResponse.getJSONObject(i);
                                Context context = getApplicationContext();

                                TextView totalActivos = (TextView) findViewById(R.id.totalActivos);
                                TextView totalConfirmados = (TextView) findViewById(R.id.totalConfirmados);
                                TextView totalMuertes = (TextView) findViewById(R.id.totalMuertes);
                                TextView nuevosConfirmados = (TextView) findViewById(R.id.nuevosConfirmados);
                                TextView nuevosMuertes = (TextView) findViewById(R.id.nuevosMuertes);
                                TextView fecha = (TextView) findViewById(R.id.fecha);
                                totalActivos.setText("creo que lo tengo que calcular");
                                totalConfirmados.setText(jobj.getString("TotalConfirmed"));
                                totalMuertes.setText(jobj.getString("TotalDeaths"));
                                nuevosConfirmados.setText(jobj.getString("NewConfirmed"));
                                nuevosMuertes.setText(jobj.getString("NewDeaths"));
                                fecha.setText(jobj.getString("Date"));
                            } // else {}
                        } catch (JSONException e) {
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
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}