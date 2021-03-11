package com.example.covidinfo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewManager;
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
        List<Country> list=db.countryDao().getAllOrderByName();
//        db.countryDao().nukeTable();
        if(list.isEmpty()) {
            Country c1 = new Country("Argentina","0","0","0","0","0");
            Country c2 = new Country("Brazil","3","7","1","2","1");
            c1.setDate(new java.sql.Date(new Date().getTime()));
            c2.setDate(new java.sql.Date(new Date().getTime()));
            db.countryDao().insertAll(c1, c2);
            list=db.countryDao().getAll();
        }
        this.searchFavsInfo(list);
//        db.close();
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
                                    for (final Country c:list){
                                        while ((i <jsonResponse.length()-1) && !jsonResponse.getJSONObject(i).getString("Country").equals(c.getName())){
                                            i++;
                                        }

                                        // Creo el objeto que tiene los datos del pais que tengo que mostrar en la view
                                        JSONObject jobj = jsonResponse.getJSONObject(i);
                                        long totalActivos = jobj.getLong("TotalConfirmed") - jobj.getLong("TotalDeaths") - jobj.getLong("TotalRecovered");
                                        String totalConfirmadosStr = "Confirmados totales: "+jobj.getString("TotalConfirmed");
                                        String totalMuertesStr = "Muertes totales: "+jobj.getString("TotalDeaths");
                                        String nuevosConfirmadosStr = "Nuevos confirmados: "+jobj.getString("NewConfirmed");
                                        String nuevosMuertesStr = "Nuevos muertos: "+jobj.getString("NewDeaths");
                                        Country countryAux = new Country(c.getName(),"Activos totales: "+ totalActivos,totalConfirmadosStr,totalMuertesStr,nuevosConfirmadosStr,nuevosMuertesStr);
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
                                        java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
                                        countryAux.setDate(sqlDate);

                                        crearViewParaPais(countryAux,i,favs,context);

//                                         Actualizar solo si los datos son mas nuevos
                                        if (c.getDate() != null && c.getDate().before(utilDate)){
                                            Toast.makeText(getApplicationContext(), "Actualizado", Toast.LENGTH_SHORT).show();

//        -----------                       //REVISAR QUE ESTO ANDE BIEN
                                            c.setTotalMuertes(countryAux.getTotalMuertes());
                                            c.setTotalConfirmados(countryAux.getTotalConfirmados());
                                            c.setTotalActivos(countryAux.getTotalActivos());
                                            c.setNuevosMuertes(countryAux.getNuevosMuertes());
                                            c.setNuevosConfirmados(countryAux.getNuevosConfirmados());
//                                            java.sql.Date sqlDate = countryAux.getDate();
                                            c.setDate(countryAux.getDate());
                                            db.countryDao().updateCountry(c);
                                        }
                                    }
                                } else {
                                    // Si el json esta vacio, cargo los datos de la DB
                                    cargarDesdeDB(list);
                                    Toast.makeText(getApplicationContext(), "No se pudo actualizar los datos", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                // Si el ocurre un error, cargo los datos de la DB
                                cargarDesdeDB(list);
                                Toast.makeText(getApplicationContext(), "No se pudo actualizar los datos, error1", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Si el servidor responde con un error, cargo los datos de la DB
                    cargarDesdeDB(list);
                    Toast.makeText(getApplicationContext(), "No se pudo actualizar los datos, error2", Toast.LENGTH_SHORT).show();
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
                        } catch (JSONException e) {
                            // Si no puedo cargar la lista de paises, borro el boton de buscar
                            TextView texto = new TextView(getApplicationContext());
                            texto.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            texto.setPadding(0,0,0,0);
                            texto.setText("No se pudo cargar la lista de paises");
                            Button b = (Button) findViewById(R.id.buscar);
                            ((ViewManager)b.getParent()).addView(texto, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            ((ViewManager)b.getParent()).removeView(b);
                            Toast.makeText(getApplicationContext(),"Error al cargar la lista de paises", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Si el servidor responde con un error, borro el boton de buscar
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

        // Creo los campos para todos los atributos a mostrar y les asigno un id
        // los defino como final para poder accederlos desde el setOnClickListener del ImageButton
        final TextView totalActivos =  new TextView(context);
        totalActivos.setId(i*10+1);
        final TextView totalConfirmados = new TextView(context);
        totalConfirmados.setId(i*10+2);
        final TextView totalMuertes = new TextView(context);
        totalMuertes.setId(i*10+3);
        final TextView nuevosConfirmados = new TextView(context);
        nuevosConfirmados.setId(i*10+4);
        final TextView nuevosMuertes = new TextView(context);
        nuevosMuertes.setId(i*10+5);
        final TextView dateView = new TextView(context);
        dateView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        dateView.setId(i*10+6);
        final ImageButton favIcon = new ImageButton(context);
        favIcon.setImageResource(R.drawable.si);
        favIcon.setPadding(0,0,0,0);
        favIcon.setBackgroundColor(Color.WHITE);
        favIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        favIcon.setAdjustViewBounds(true);
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
                    Toast.makeText(getApplicationContext(), "Agregado a favoritos", Toast.LENGTH_SHORT).show();
                } else {
                    db.countryDao().delete(country);
                    favIcon.setImageResource(R.drawable.no);
                    Toast.makeText(getApplicationContext(), "Eliminado de favoritos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageButton unfold = new ImageButton(context);
        unfold.setImageResource(R.drawable.des);
        unfold.setPadding(0,30,20,0);
        unfold.setBackgroundColor(Color.WHITE);
        unfold.setMaxHeight(70);
        unfold.setMaxWidth(70);
        unfold.setScaleType(ImageView.ScaleType.FIT_CENTER);
        unfold.setAdjustViewBounds(true);
        unfold.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (layoutContent.getVisibility() == View.GONE) {
                    //            animar(true);
                    layoutContent.setVisibility(View.VISIBLE);
                } else{
                    //            animar(false);
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
        dateView.setText(day +"/"+ month +"/"+ year +" "+ hour +":"+ minute);

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
        //ACTUALIZAR LOS DATOS DE LOS FAVORITOS
        totalActivos.setText(c.getTotalActivos());
        totalConfirmados.setText(c.getTotalConfirmados());
        totalMuertes.setText(c.getTotalMuertes());
        nuevosConfirmados.setText(c.getNuevosConfirmados());
        nuevosMuertes.setText(c.getNuevosMuertes());
        layoutContent.addView(totalActivos);
        layoutContent.addView(totalConfirmados);
        layoutContent.addView(totalMuertes);
        layoutContent.addView(nuevosConfirmados);
        layoutContent.addView(nuevosMuertes);
    }

//    private void animar(boolean mostrar)
//    {
//        AnimationSet set = new AnimationSet(true);
//        Animation animation = null;
//        if (mostrar)
//        {
//            //desde la esquina inferior derecha a la superior izquierda
//            animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
//        }
//        else
//        {    //desde la esquina superior izquierda a la esquina inferior derecha
//            animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
//        }
//        //duración en milisegundos
//        animation.setDuration(500);
//        set.addAnimation(animation);
//        LayoutAnimationController controller = new LayoutAnimationController(set, 0.25f);
//
//        layoutAnimado.setLayoutAnimation(controller);
//        layoutAnimado.startAnimation(animation);
//    }
}