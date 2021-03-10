package com.example.covidinfo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
        } else {
            errorAlCargar();
        }

        this.searchFavInfo();
    }

    public void toggleFav(View view) {
        TextView pais = (TextView)findViewById(R.id.pais);
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        Country country=db.countryDao().findByName((String) pais.getText());
        ImageButton favIcon = findViewById(R.id.favIcon);
        if(country==null) {
            String totalActivos = ((TextView) findViewById(R.id.totalActivos)).getText().toString();
            String totalConfirmados = ((TextView) findViewById(R.id.totalConfirmados)).getText().toString();
            String totalMuertes = ((TextView) findViewById(R.id.totalMuertes)).getText().toString();
            String nuevosConfirmados = ((TextView) findViewById(R.id.nuevosConfirmados)).getText().toString();
            String nuevosMuertes = ((TextView) findViewById(R.id.nuevosMuertes)).getText().toString();
            Country c1 = new Country(pais.getText().toString(),totalActivos,totalConfirmados,totalMuertes,nuevosConfirmados,nuevosMuertes);

            String strDate = ((TextView) findViewById(R.id.fecha)).getText().toString();
            Date utilDate; // = new Date(strDate);
            try {
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyyy HH:mm");
                utilDate = format.parse(strDate);
            }
            catch(ParseException pe) {
                throw new IllegalArgumentException(pe);
            }
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
            c1.setDate(sqlDate);
            db.countryDao().insert(c1);
            favIcon.setImageResource(R.drawable.si);
            Toast.makeText(getApplicationContext(), "Agregado a favoritos", Toast.LENGTH_SHORT).show();
        } else {
            db.countryDao().delete(country);
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
        final String country;
        country = (String) ((TextView) findViewById(R.id.pais)).getText();

        //----------------------
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        final Country pais=db.countryDao().findByName(country);
        ImageButton favIcon = findViewById(R.id.favIcon);
        if(pais!=null) {
            favIcon.setImageResource(R.drawable.si);
        } else {
            favIcon.setImageResource(R.drawable.no);
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
                                Long activos = jobj.getLong("TotalConfirmed") - jobj.getLong("TotalDeaths") - jobj.getLong("TotalRecovered");
                                totalActivos.setText("Activos totales: "+activos.toString());
                                totalConfirmados.setText("Confirmados totales: "+jobj.getString("TotalConfirmed"));
                                totalMuertes.setText("Muertes totales: "+jobj.getString("TotalDeaths"));
                                nuevosConfirmados.setText("Nuevos confirmados: "+jobj.getString("NewConfirmed"));
                                nuevosMuertes.setText("Nuevos muertos: "+jobj.getString("NewDeaths"));

                                String strDate = jobj.getString("Date");
                                strDate=strDate.replace("T"," ");
                                strDate=strDate.replace("Z","");
                                Date utilDate; // = new Date(strDate);
                                try {
                                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");
                                    utilDate = format.parse(strDate);
                                }
                                catch(ParseException pe) {
                                    throw new IllegalArgumentException(pe);
                                }
                                Calendar calendar = new GregorianCalendar();
                                calendar.setTime(utilDate);
                                Integer year = calendar.get(Calendar.YEAR);
                                Integer month = calendar.get(Calendar.MONTH) + 1;
                                Integer day = calendar.get(Calendar.DAY_OF_MONTH);
                                Integer hour = calendar.get(Calendar.HOUR);
                                Integer minute = calendar.get(Calendar.MINUTE);
                                fecha.setText(day.toString()+"/"+month.toString()+"/"+year.toString()+" "+hour.toString()+":"+minute.toString());

    // DEBERIA ACTUALIZAR SI ENCUENTRA INFO NUEVA
                                if (pais!=null && pais.getDate().before(utilDate)){ //
//                                    Toast.makeText(getApplicationContext(), "Actualizado", Toast.LENGTH_SHORT).show();
//        -----------                       //REVISAR QUE ESTO ANDE BIEN
                                    pais.setTotalMuertes(jobj.getString("TotalDeaths"));
                                    pais.setTotalConfirmados(jobj.getString("TotalConfirmed"));
                                    pais.setTotalActivos(activos.toString());
                                    pais.setNuevosMuertes(jobj.getString("NewDeaths"));
                                    pais.setNuevosConfirmados(jobj.getString("NewConfirmed"));
                                    java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
                                    pais.setDate(sqlDate);
                                    AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                                    db.countryDao().updateCountry(pais);
                                }
                            }
                        } catch (JSONException e) {
                            // Cargar desde la db
                            // No va a estar en la db necesariamente si no es favorito...
                            if(pais!=null) {
                                cargarInfoDesdeDB(pais);
                            } else {
                                errorAlCargar();
                            }
//                            errorAlCargar();
                            Toast.makeText(getApplicationContext(), "Error3", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Cargar desde la db
                // No va a estar en la db necesariamente si no es favorito...
                if(pais!=null) {
                    cargarInfoDesdeDB(pais);
                } else {
                    errorAlCargar();
                }
                Toast.makeText(getApplicationContext(), "Error4", Toast.LENGTH_SHORT).show();
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void cargarInfoDesdeDB(Country pais){
        if (pais!=null) {
            TextView totalActivos = (TextView) findViewById(R.id.totalActivos);
            TextView totalConfirmados = (TextView) findViewById(R.id.totalConfirmados);
            TextView totalMuertes = (TextView) findViewById(R.id.totalMuertes);
            TextView nuevosConfirmados = (TextView) findViewById(R.id.nuevosConfirmados);
            TextView nuevosMuertes = (TextView) findViewById(R.id.nuevosMuertes);
            TextView fecha = (TextView) findViewById(R.id.fecha);

            totalActivos.setText(pais.getTotalActivos());
            totalConfirmados.setText(pais.getTotalConfirmados());
            totalMuertes.setText(pais.getTotalMuertes());
            nuevosConfirmados.setText(pais.getNuevosConfirmados());
            nuevosMuertes.setText(pais.getNuevosMuertes());

            java.sql.Date sqlDate = pais.getDate();
            Date utilDate = new Date(sqlDate.getTime());
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(utilDate);
            Integer year = calendar.get(Calendar.YEAR);
            Integer month = calendar.get(Calendar.MONTH) + 1;
            Integer day = calendar.get(Calendar.DAY_OF_MONTH);
            Integer hour = calendar.get(Calendar.HOUR);
            Integer minute = calendar.get(Calendar.MINUTE);
            fecha.setText(day.toString() + "/" + month.toString() + "/" + year.toString() + " " + hour.toString() + ":" + minute.toString());
        }
    }

    private void errorAlCargar(){
        Toast.makeText(getApplicationContext(), "No se pudo obtener la info del pais", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}