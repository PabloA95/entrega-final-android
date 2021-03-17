package com.example.covidinfo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        Objects.requireNonNull(getSupportActionBar()).hide(); // hide the title bar
        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(this);
        queue.start();

//        this.searchCountriesList();
        AppDatabase db = AppDatabase.getInstance(this);
        List<Country> list=db.countryDao().getAllOrderByName();

//        // Test
//        if(list.isEmpty()) {
//            Country c1 = new Country("Argentina",0L,0L,0L,0L,0L);
//            Country c2 = new Country("Brazil",3L,7L,1L,2L,1L);
//            c1.setDate(new java.sql.Date(new Date().getTime()));
//            c2.setDate(new java.sql.Date(new Date().getTime()));
//            db.countryDao().insertAll(c1, c2);
//            list=db.countryDao().getAll();
//        }

        this.searchFavsInfo(list);
    }

    private void searchFavsInfo(final List<Country> list) {
        final LinearLayout favs = (LinearLayout) findViewById(R.id.favs);
        // Instantiate the RequestQueue.
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
                                    int i=0;
                                    AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                                    Context context = getApplicationContext();
                                    String auxCountry = jsonResponse.getJSONObject(0).getString("Country");
                                    ArrayList<String> countriesSpinner = new ArrayList<String>();
                                    for (final Country c:list){
                                        while ((i <jsonResponse.length()-1) && !auxCountry.equals(c.getName()) && (auxCountry.compareTo(c.getName())<0)){
                                            i++;
                                            auxCountry = jsonResponse.getJSONObject(i).getString("Country");
                                            countriesSpinner.add(auxCountry);
                                        }

                                        if (auxCountry.equals(c.getName())) {
                                            // Creo el objeto que tiene los datos del pais que tengo que mostrar en la view
                                            JSONObject jobj = jsonResponse.getJSONObject(i);
                                            long totalConfirmados = jobj.getLong("TotalConfirmed");
                                            long totalMuertes = jobj.getLong("TotalDeaths");
                                            long nuevosConfirmados = jobj.getLong("NewConfirmed");
                                            long nuevosMuertes = jobj.getLong("NewDeaths");
                                            long totalActivos = totalConfirmados - totalMuertes - jobj.getLong("TotalRecovered");
//NECESITO CREAR UN OBJETO NUEVO? REVISAR ESTO -> EN C TENGO EL OBJETO...
                                            Country countryAux = new Country(c.getName(), totalActivos, totalConfirmados, totalMuertes, nuevosConfirmados, nuevosMuertes);
                                            String strDate = jobj.getString("Date");
                                            strDate = strDate.replace("T", " ");
                                            strDate = strDate.replace("Z", "");
                                            Date utilDate; // = new Date(strDate);
                                            try {
                                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");
                                                format.setTimeZone(TimeZone.getTimeZone(TimeZone.getDefault().getDisplayName()));
                                                utilDate = format.parse(strDate);
                                                Calendar calendar = new GregorianCalendar();
                                                calendar.setTime(utilDate);
                                            } catch (ParseException pe) {
                                                throw new IllegalArgumentException(pe);
                                            }
                                            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
                                            countryAux.setDate(sqlDate);

                                            crearViewParaPais(countryAux, i, favs, context);

//                                      Actualizar solo si los datos son mas nuevos
                                            if (c.getDate() != null && c.getDate().before(utilDate)) {
                                                c.setTotalMuertes(countryAux.getTotalMuertes());
                                                c.setTotalConfirmados(countryAux.getTotalConfirmados());
                                                c.setTotalActivos(countryAux.getTotalActivos());
                                                c.setNuevosMuertes(countryAux.getNuevosMuertes());
                                                c.setNuevosConfirmados(countryAux.getNuevosConfirmados());
                                                c.setDate(countryAux.getDate());
                                                db.countryDao().updateCountry(c);
                                                Toast.makeText(getApplicationContext(), "Actualizado", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Error al recuperar la informacion de "+auxCountry, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    for (int j = i; j < jsonResponse.length(); j++) {
                                        countriesSpinner.add(jsonResponse.getJSONObject(j).getString("Country"));
                                    }
                                    loadCountriesList(countriesSpinner);
                                } else {
                                    // Si el json esta vacio, cargo los datos de la DB
                                    cargarDesdeDB(list);
                                    errorCargandoLaLista();
                                    Toast.makeText(getApplicationContext(), getString(R.string.jsonArrayVacio), Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                // Si el ocurre un error, cargo los datos de la DB
                                cargarDesdeDB(list);
                                errorCargandoLaLista();
                                Toast.makeText(getApplicationContext(), getString(R.string.jsonException), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Si el servidor responde con un error, cargo los datos de la DB
                    cargarDesdeDB(list);
                    errorCargandoLaLista();
                    Toast.makeText(getApplicationContext(), getString(R.string.volleryError), Toast.LENGTH_SHORT).show();
                }
            });

            // Add the request to the RequestQueue.
            queue.add(stringRequest);
    }

    private void loadCountriesList(ArrayList<String> countriesSpinner){
        Spinner s = (Spinner) findViewById(R.id.countries);
        Collections.sort(countriesSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.spinner_style, countriesSpinner);
        adapter.setDropDownViewResource(R.layout.spinner_style);
        s.setAdapter(adapter);
        ((Button) findViewById(R.id.buscar)).setVisibility(View.VISIBLE);
        ((Button) findViewById(R.id.reload)).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.listError)).setVisibility(View.GONE);
    }

    public void verDetalles(View view) {
        Intent i = new Intent(this,CountryDetails.class);
        Spinner country = (Spinner) findViewById(R.id.countries);
        String param = country.getSelectedItem().toString();
        i.putExtra("country",param);
        startActivity(i);
    }

    public void cargarDesdeDB(List<Country> list){
        LinearLayout favs = (LinearLayout) findViewById(R.id.favs);
        int i=0;
        Context context = getApplicationContext();
        for (Country c:list){
            crearViewParaPais(c,i, favs,context);
            i++;
        }
    }

    public void recargarLista(View view) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://api.covid19api.com/summary";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray jsonResponse;
                        ArrayList<String> countriesSpinner = new ArrayList<String>();
                        try {
                            jsonResponse = (new JSONObject(response)).getJSONArray("Countries");
                            for (int i = 0; i < jsonResponse.length(); i++) {
                                countriesSpinner.add(jsonResponse.getJSONObject(i).getString("Country"));
                            }

                            loadCountriesList(countriesSpinner);

                        } catch (JSONException e) {
                            errorCargandoLaLista();
                            Toast.makeText(getApplicationContext(), getString(R.string.errorCargarListaPaises), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Si el servidor responde con un error, borro el boton de buscar
                errorCargandoLaLista();
                Toast.makeText(getApplicationContext(), getString(R.string.errorRespuestaServidor), Toast.LENGTH_SHORT).show();
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void errorCargandoLaLista(){
        Button r = (Button) findViewById(R.id.reload);
        r.setVisibility(View.VISIBLE);
        // Si no puedo cargar la lista de paises, borro el boton de buscar
        TextView texto = (TextView) findViewById(R.id.listError);
        texto.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        texto.setPadding(25,0,0,0);
        texto.setText(getString(R.string.errorCargarLista));
        texto.setVisibility(View.VISIBLE);
        Button b = (Button) findViewById(R.id.buscar);
        b.setVisibility(View.GONE);
    }

    private void crearViewParaPais(final Country c, int i, LinearLayout favs, Context context){
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10,10,10,10);
        layout.setLayoutParams(layoutParams);
        RelativeLayout hLayout = new RelativeLayout(context);
        layout.addView(hLayout);
        final LinearLayout layoutContent = new LinearLayout(context);
        layoutContent.setOrientation(LinearLayout.VERTICAL);
        layoutParams.setMargins(10,10,10,10);
        layoutContent.setLayoutParams(layoutParams);
        layoutContent.setVisibility(View.GONE);

        final TextView titleView = new TextView(context);
        titleView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        titleView.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),CountryDetails.class);
                i.putExtra("country",titleView.getText());
                startActivity(i);
            }
        });

        int myColor = getResources().getColor(R.color.textColor);
        titleView.setTextColor(myColor);
        // Creo los campos para todos los atributos a mostrar y les asigno un id
        // los defino como final para poder accederlos desde el setOnClickListener del ImageButton
        final TextView totalActivos =  new TextView(context);
        totalActivos.setId(i*10+1);
        totalActivos.setTextColor(myColor);
        final TextView totalConfirmados = new TextView(context);
        totalConfirmados.setId(i*10+2);
        totalConfirmados.setTextColor(myColor);
        final TextView totalMuertes = new TextView(context);
        totalMuertes.setId(i*10+3);
        totalMuertes.setTextColor(myColor);
        final TextView nuevosConfirmados = new TextView(context);
        nuevosConfirmados.setId(i*10+4);
        nuevosConfirmados.setTextColor(myColor);
        final TextView nuevosMuertes = new TextView(context);
        nuevosMuertes.setId(i*10+5);
        nuevosMuertes.setTextColor(myColor);
        final TextView dateView = new TextView(context);
        dateView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        dateView.setId(i*10+6);
        dateView.setTextColor(myColor);
        final ImageButton favIcon = new ImageButton(context);
        favIcon.setImageResource(R.drawable.si);
        favIcon.setPadding(0,0,0,0);
        favIcon.setBackgroundColor(Color.WHITE);
        favIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        favIcon.setAdjustViewBounds(true);
        favIcon.setBackgroundColor(Color.TRANSPARENT);
        favIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                // Funcion para realizar el toggleFav
                Country country=db.countryDao().findByName((String) c.getName());
//                        Toast.makeText(getApplicationContext(), c.getName(), Toast.LENGTH_SHORT).show();
                if(country==null) {
                    db.countryDao().insert(c);
                    favIcon.setImageResource(R.drawable.si);
                    Toast.makeText(getApplicationContext(), getString(R.string.paisAgregado), Toast.LENGTH_SHORT).show();
                } else {
                    db.countryDao().delete(country);
                    favIcon.setImageResource(R.drawable.no);
                    Toast.makeText(getApplicationContext(), getString(R.string.paisEliminado), Toast.LENGTH_SHORT).show();
                }
            }
        });

        final ImageButton unfold = new ImageButton(context);
        unfold.setImageResource(R.drawable.des);
        unfold.setPadding(0,30,20,0);
        unfold.setBackgroundColor(Color.WHITE);
        unfold.setMaxHeight(70);
        unfold.setMaxWidth(70);
        unfold.setScaleType(ImageView.ScaleType.FIT_CENTER);
        unfold.setAdjustViewBounds(true);
        unfold.setBackgroundColor(Color.TRANSPARENT);
        unfold.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (layoutContent.getVisibility() == View.GONE) {
                    unfold.setImageResource(R.drawable.pleg);
                    layoutContent.setVisibility(View.VISIBLE);
                } else{
                    unfold.setImageResource(R.drawable.des);
                    layoutContent.setVisibility(View.GONE);
                }
            }
        });

        LinearLayout layoutTitle = new LinearLayout(context);
        layoutTitle.setOrientation(LinearLayout.HORIZONTAL);
        layoutContent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        titleView.setPadding(20,0,0,30);
        layoutTitle.addView(unfold);
        layoutTitle.addView(titleView);
        hLayout.addView(layoutTitle);
        int textSize = 20;
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(textSize*4,textSize*4);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, favIcon.getId());
        hLayout.addView(favIcon, lp);
        layoutContent.addView(dateView);
        layout.addView(layoutContent);
        favs.addView(layout);

        titleView.setText(c.getName());
        java.sql.Date sqlDate = c.getDate();
        Date utilDate = new Date(sqlDate.getTime());
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(utilDate);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        dateView.setText(String.format("%d/%d/%d %d:%d", day, month, year, hour, minute));

        LinearLayout.LayoutParams dataParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        nuevosMuertes.setLayoutParams(dataParams);
        totalActivos.setLayoutParams(dataParams);
        totalConfirmados.setLayoutParams(dataParams);
        totalMuertes.setLayoutParams(dataParams);
        nuevosConfirmados.setLayoutParams(dataParams);

        titleView.setTextSize(textSize+10);
        dateView.setTextSize(textSize+5);
        nuevosMuertes.setTextSize(textSize);
        totalActivos.setTextSize(textSize);
        totalConfirmados.setTextSize(textSize);
        totalMuertes.setTextSize(textSize);
        nuevosConfirmados.setTextSize(textSize);

        totalActivos.setText(getString(R.string.activosTotales)+c.getTotalActivos());
        totalConfirmados.setText(getString(R.string.confirmadosTotales)+c.getTotalConfirmados());
        totalMuertes.setText(getString(R.string.muertesTotales)+c.getTotalMuertes());
        nuevosConfirmados.setText(getString(R.string.nuevosConfirmados)+c.getNuevosConfirmados());
        nuevosMuertes.setText(getString(R.string.nuevosMuertos)+c.getNuevosMuertes());

        layoutContent.addView(totalActivos);
        layoutContent.addView(totalConfirmados);
        layoutContent.addView(totalMuertes);
        layoutContent.addView(nuevosConfirmados);
        layoutContent.addView(nuevosMuertes);
    }

}