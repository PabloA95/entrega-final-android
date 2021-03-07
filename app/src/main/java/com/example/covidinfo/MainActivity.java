package com.example.covidinfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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
        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database-name").fallbackToDestructiveMigration().allowMainThreadQueries().build();

        List<Country> list=db.countryDao().getAll();
        if(list.isEmpty()) {
            db.countryDao().nukeTable();
            Country c1 = new Country("Argentina");
            Country c2 = new Country("Brazil");
            db.countryDao().insertAll(c1, c2);
            list=db.countryDao().getAll();
        }

        this.searchFavsInfo(list);
        db.close();
    }

    private void searchFavsInfo(List<Country> list) {
        final LinearLayout favs = (LinearLayout) findViewById(R.id.favs);
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://api.covid19api.com/total/dayone/country/";

        int i=0;
        for (final Country c:list){
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(10,10,10,10);
            layout.setLayoutParams(layoutParams);
//------------------------------------
            RelativeLayout hLayout = new RelativeLayout(this);
//            hLayout.setOrientation(LinearLayout.HORIZONTAL);
//            LinearLayout.LayoutParams hLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
//            layout.setLayoutParams(hLayoutParams);
            layout.addView(hLayout);

            final TextView titleView = new TextView(this);
            titleView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            titleView.setText("Hallo Welt!");
            titleView.setId(i);
            titleView.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    Intent i = new Intent(getApplicationContext(),CountryDetails.class);
                    i.putExtra("country",titleView.getText());
                    startActivity(i);
//                    Toast.makeText(getApplicationContext(), "Exito", Toast.LENGTH_SHORT).show();
                }
            });

            final ImageButton favIcon = new ImageButton(this);
//            favIcon.setLayoutParams(new LinearLayout.LayoutParams(50,50));
            Drawable d = getResources().getDrawable(R.drawable.si);
            favIcon.setPadding(0,0,0,0);
            favIcon.setBackgroundColor(Color.WHITE);

            favIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            favIcon.setAdjustViewBounds(true);
            favIcon.setImageDrawable(d);
            favIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    TextView pais = (TextView)findViewById(R.id.pais);
                    AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database-name").fallbackToDestructiveMigration().allowMainThreadQueries().build();
                    Country country=db.countryDao().findByName((String) c.getName());
                    if(country==null) {
                        Country c1 = new Country((String) c.getName());
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
//                    Toast.makeText(getApplicationContext(),"search button is Clicked", Toast.LENGTH_LONG).show();
                }
            });


            hLayout.addView(titleView);

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(50,50);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, favIcon.getId());
            hLayout.addView(favIcon, lp);

//            hLayout.addView(favIcon);

            final TextView dateView = new TextView(this);
            dateView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            dateView.setText("Hallo Welt!");
            dateView.setId(i);
            layout.addView(dateView);

            favs.addView(layout);
//            setContentView(layout);
            i++;
            titleView.setText(c.getName());
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url+c.getName(),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            JSONArray jsonResponse;
                            try {
                                jsonResponse = new JSONArray(response);
                                if (jsonResponse.length() > 0) {
                                    titleView.setText(jsonResponse.getJSONObject(0).getString("Country"));

//                                Toast.makeText(getApplicationContext(), "url"+c.getName(), Toast.LENGTH_SHORT).show();
//CONTROLAR QUE TENGA ALGUN ELEMENTO -> Antarctica no devuelve nada!  getJSONObject(0) -> no tiene posicion 0
                                    dateView.setText(jsonResponse.getJSONObject(0).getString("Date"));
                                } //else {}

                            } catch (JSONException e) {
                                titleView.setText(c.getName());
                                // Cargar desde la db
                                //remover boton "buscar"
                                View v = (View) findViewById(R.id.buscar);
                                ((ViewManager)v.getParent()).removeView(v);
                                Toast.makeText(getApplicationContext(), "Error1", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Cargar desde la db
                    titleView.setText(c.getName());
                    Toast.makeText(getApplicationContext(), "Error2", Toast.LENGTH_SHORT).show();
                }
            });
//            Toast.makeText(getApplicationContext(), "https://api.covid19api.com/total/dayone/country/"+c.getName(), Toast.LENGTH_SHORT).show();
            queue.add(stringRequest);
        }
        // Add the request to the RequestQueue.

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
                            Toast.makeText(getApplicationContext(),"Error al cargar la lista de paises", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                textView.setText("That didn't work!");
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