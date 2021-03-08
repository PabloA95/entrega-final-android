package com.example.covidinfo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(this);
        queue.start();

        this.searchCountriesList();
//        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database-name").fallbackToDestructiveMigration().allowMainThreadQueries().build();
        AppDatabase db = AppDatabase.getInstance(this);
        List<Country> list=db.countryDao().getAll();
        if(list.isEmpty()) {
            db.countryDao().nukeTable();
            Country c1 = new Country("Argentina");
            Country c2 = new Country("Brazil");
            db.countryDao().insertAll(c1, c2);
            list=db.countryDao().getAll();
        }
        this.searchFavsInfo(list);
//        db.close();
    }

    private void searchFavsInfo(final List<Country> list) {
        final LinearLayout favs = (LinearLayout) findViewById(R.id.favs);
//        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://api.covid19api.com/summary";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
             new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            JSONArray jsonResponse;

                            try {
                                jsonResponse = (new JSONObject(response)).getJSONArray("Countries");
                                if (jsonResponse.length() > 0) {
                                    for (final Country c:list){
                                        Integer i=0;
                                        while ((i <jsonResponse.length()-1) && !jsonResponse.getJSONObject(i).getString("Country").equals(c.getName())){
                                            i++;
                                        }
                                        JSONObject jobj = jsonResponse.getJSONObject(i);
                                        Context context = getApplicationContext();
//                                        Toast.makeText(getApplicationContext(), jobj.getString("Country") , Toast.LENGTH_SHORT).show();

                                        LinearLayout layout = new LinearLayout(context);
                                        layout.setOrientation(LinearLayout.VERTICAL);
                                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                        layoutParams.setMargins(10,10,10,10);
                                        layout.setLayoutParams(layoutParams);

                                        RelativeLayout hLayout = new RelativeLayout(context);
                                        layout.addView(hLayout);

                                        final TextView titleView = new TextView(context);
                                        titleView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                        titleView.setText("Hallo Welt!");
                                        titleView.setId(i);
                                        titleView.setOnClickListener(new Button.OnClickListener() {
                                            public void onClick(View v) {
                                                Intent i = new Intent(getApplicationContext(),CountryDetails.class);
                                                i.putExtra("country",titleView.getText());
                                                startActivity(i);
                                            }
                                        });

                                        final ImageButton favIcon = new ImageButton(context);
                                        Drawable d = getResources().getDrawable(R.drawable.si);
                                        favIcon.setPadding(0,0,0,0);
                                        favIcon.setBackgroundColor(Color.WHITE);
                                        favIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                        favIcon.setAdjustViewBounds(true);
                                        favIcon.setImageDrawable(d);
                                        favIcon.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                                                Country country=db.countryDao().findByName((String) c.getName());
                                                if(country==null) {
                                                    Country c1 = new Country((String) c.getName());
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
                                            }
                                        });

                                        hLayout.addView(titleView);
                                        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(50,50);
                                        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, favIcon.getId());
                                        hLayout.addView(favIcon, lp);

                                        final TextView dateView = new TextView(context);
                                        dateView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                        dateView.setText("Hallo Welt!");
                                        dateView.setId(i);
                                        layout.addView(dateView);
                                        favs.addView(layout);

                                        titleView.setText(jobj.getString("Country"));
                                        dateView.setText(jobj.getString("Date"));

                                        LinearLayout.LayoutParams dataParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                        TextView totalActivos =  new TextView(context);
                                        TextView totalConfirmados = new TextView(context);
                                        TextView totalMuertes = new TextView(context);
                                        TextView nuevosConfirmados = new TextView(context);
                                        TextView nuevosMuertes = new TextView(context);
                                        nuevosMuertes.setLayoutParams(dataParams);
                                        totalActivos.setLayoutParams(dataParams);
                                        totalConfirmados.setLayoutParams(dataParams);
                                        totalMuertes.setLayoutParams(dataParams);
                                        nuevosConfirmados.setLayoutParams(dataParams);
                                        //ACTUALIZAR LOS DATOS DE LOS FAVORITOS
                                        totalActivos.setText("creo que lo tengo que calcular");
                                        totalConfirmados.setText(jobj.getString("TotalConfirmed"));
                                        totalMuertes.setText(jobj.getString("TotalDeaths"));
                                        nuevosConfirmados.setText(jobj.getString("NewConfirmed"));
                                        nuevosMuertes.setText(jobj.getString("NewDeaths"));
                                        layout.addView(totalActivos);
                                        layout.addView(totalConfirmados);
                                        layout.addView(totalMuertes);
                                        layout.addView(nuevosConfirmados);
                                        layout.addView(nuevosMuertes);
                                    }
                                } //else {}

                            } catch (JSONException e) {
                                //CARGAR DESDE LA DB
                                Toast.makeText(getApplicationContext(), "Error1", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Cargar desde la db

                    Toast.makeText(getApplicationContext(), "Error2", Toast.LENGTH_SHORT).show();
                }
            });
            // Add the request to the RequestQueue.
            queue.add(stringRequest);
    }

    private void searchCountriesList(){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://api.covid19api.com/countries";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray jsonResponse;
                        ArrayList<String> countriesSpinner = new ArrayList<String>();
                        try {
                            jsonResponse = new JSONArray(response);
                            for (int i = 0; i < jsonResponse.length(); i++) {
                                countriesSpinner.add(jsonResponse.getJSONObject(i).getString("Country"));
                            }

                            Spinner s = (Spinner) findViewById(R.id.countries);
                            Collections.sort(countriesSpinner);
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                                    android.R.layout.simple_spinner_item, countriesSpinner);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            s.setAdapter(adapter);
                            s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id)
                                {
//                                    Toast.makeText(adapterView.getContext(), (String) adapterView.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent)
                                {
                                    // vacio

                                }
                            });
                        } catch (JSONException e) {
                            Button b = (Button) findViewById(R.id.buscar);
                            ((ViewManager)b.getParent()).removeView(b);
                            Toast.makeText(getApplicationContext(),"Error al cargar la lista de paises", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                TextView texto = new TextView(getApplicationContext());
                texto.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                texto.setPadding(0,0,0,0);
                texto.setText("No se pudo cargar la lista de paises");
                Button b = (Button) findViewById(R.id.buscar);
                ((ViewManager)b.getParent()).addView(texto, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                ((ViewManager)b.getParent()).removeView(b);
                Toast.makeText(getApplicationContext(),"Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void verDetalles(View view) {
        Intent i = new Intent(this,CountryDetails.class);
        Spinner country = (Spinner) findViewById(R.id.countries);
        String param = country.getSelectedItem().toString();;
        i.putExtra("country",param);
        startActivity(i);
    }

}