package com.example.covidinfo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.TimeZone;

public class CountryDetails extends AppCompatActivity {

    ArrayList<Entry> x;
    ArrayList<Entry> x2;
    ArrayList<Entry> x3;
    private LineChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        Objects.requireNonNull(getSupportActionBar()).hide(); // hide the title bar
        setContentView(R.layout.activity_country_details);

        Bundle datos = getIntent().getExtras();
        if (datos != null)
        {
            String countryName = datos.getString("country");
            TextView pais = (TextView)findViewById(R.id.pais);
            pais.setText(countryName);
        } else {
            errorAlCargar(getString(R.string.errorInfoPais));
        }

        this.searchFavInfo();

        x = new ArrayList<Entry>();
        x2 = new ArrayList<Entry>();
        x3 = new ArrayList<Entry>();
        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setDrawGridBackground(false);
        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setPinchZoom(true);
        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setTextSize(15);
        xl.setDrawAxisLine(true);
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setAxisMinimum(0);
        leftAxis.setTextColor(Color.WHITE);
        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);
        l.setTextSize(15);
        l.setXOffset(15);
    }

    public void toggleFav(View view) {
        TextView pais = (TextView)findViewById(R.id.pais);
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        Country country=db.countryDao().findByName((String) pais.getText());
        ImageButton favIcon = findViewById(R.id.favIcon);
        if(country==null) {
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
            Long totalActivos = Long.valueOf(((TextView) findViewById(R.id.totalActivos)).getText().toString().replaceAll("[a-zA-Z :]", ""));
            Long totalConfirmados = Long.valueOf(((TextView) findViewById(R.id.totalConfirmados)).getText().toString().replaceAll("[a-zA-Z :]", ""));
            Long totalMuertes = Long.valueOf(((TextView) findViewById(R.id.totalMuertes)).getText().toString().replaceAll("[a-zA-Z :]", ""));
            Long nuevosConfirmados = Long.valueOf(((TextView) findViewById(R.id.nuevosConfirmados)).getText().toString().replaceAll("[a-zA-Z :]", ""));
            Long nuevosMuertes = Long.valueOf(((TextView) findViewById(R.id.nuevosMuertes)).getText().toString().replaceAll("[a-zA-Z :]", ""));
            Country c1 = new Country(pais.getText().toString(),sqlDate,totalActivos,totalConfirmados,totalMuertes,nuevosConfirmados,nuevosMuertes);

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
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        final Country pais=db.countryDao().findByName(country);
        ImageButton favIcon = findViewById(R.id.favIcon);
        if(pais!=null) {
            favIcon.setImageResource(R.drawable.si);
        } else {
            favIcon.setImageResource(R.drawable.no);
        }

        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray jsonResponse;
                        try {
                            jsonResponse = (new JSONObject(response)).getJSONArray("Countries");
                            if (jsonResponse.length() > 0) {
                                String auxCountry = jsonResponse.getJSONObject(0).getString("Country");
                                int i = 0;
                                while ((i <jsonResponse.length()-1) && !auxCountry.equals(country) && (auxCountry.compareTo(country)<0)){
                                    i++;
                                    auxCountry = jsonResponse.getJSONObject(i).getString("Country");
                                }
                                if (auxCountry.equals(country)) {
                                    // CONTROLAR SI POR MOTIVO RARO NO LO ENCUENTRA
                                    JSONObject jobj = jsonResponse.getJSONObject(i);
                                    Context context = getApplicationContext();

                                    TextView totalActivos = findViewById(R.id.totalActivos);
                                    TextView totalConfirmados = findViewById(R.id.totalConfirmados);
                                    TextView totalMuertes = findViewById(R.id.totalMuertes);
                                    TextView nuevosConfirmados = findViewById(R.id.nuevosConfirmados);
                                    TextView nuevosMuertes = findViewById(R.id.nuevosMuertes);
                                    TextView fecha = findViewById(R.id.fecha);
                                    Long activos = jobj.getLong("TotalConfirmed") - jobj.getLong("TotalDeaths") - jobj.getLong("TotalRecovered");
                                    totalActivos.setText(getString(R.string.activosTotales) + activos.toString());
                                    totalConfirmados.setText(getString(R.string.confirmadosTotales) + jobj.getString("TotalConfirmed"));
                                    totalMuertes.setText(getString(R.string.muertesTotales) + jobj.getString("TotalDeaths"));
                                    nuevosConfirmados.setText(getString(R.string.nuevosConfirmados) + jobj.getString("NewConfirmed"));
                                    nuevosMuertes.setText(getString(R.string.nuevosMuertos) + jobj.getString("NewDeaths"));

                                    String strDate = jobj.getString("Date");
                                    strDate = strDate.replace("T", " ");
                                    strDate = strDate.replace("Z", "");
                                    Date utilDate; // = new Date(strDate);
                                    try {
                                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");
                                        format.setTimeZone(TimeZone.getTimeZone(TimeZone.getDefault().getDisplayName()));
                                        utilDate = format.parse(strDate);
                                    } catch (ParseException pe) {
                                        throw new IllegalArgumentException(pe);
                                    }
                                    Calendar calendar = new GregorianCalendar();
                                    calendar.setTime(utilDate);
                                    int year = calendar.get(Calendar.YEAR);
                                    int month = calendar.get(Calendar.MONTH) + 1;
                                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                                    int hour = calendar.get(Calendar.HOUR);
                                    int minute = calendar.get(Calendar.MINUTE);
                                    fecha.setText(String.format("%d/%d/%d %d:%d", day, month, year, hour, minute));

                                    // DEBERIA ACTUALIZAR SI ENCUENTRA INFO NUEVA
                                    if (pais != null && pais.getDate().before(utilDate)) { //
                                        pais.setTotalMuertes(jobj.getLong("TotalDeaths"));
                                        pais.setTotalConfirmados(jobj.getLong("TotalConfirmed"));
                                        pais.setTotalActivos(activos);
                                        pais.setNuevosMuertes(jobj.getLong("NewDeaths"));
                                        pais.setNuevosConfirmados(jobj.getLong("NewConfirmed"));
                                        java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
                                        pais.setDate(sqlDate);
                                        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                                        db.countryDao().updateCountry(pais);
                                    }
                                    drawChart();
                                } else {
//                                    errorAlCargar("Error al recuperar la informacion de "+country);
                                    cargarInfoDesdeDB(pais,"Error al recuperar la informacion de "+country);
                                }
                            } else {
                                cargarInfoDesdeDB(pais,getString(R.string.errorInfoPais));
                            }

                        } catch (JSONException e) {
                            // Cargar desde la db
                            // No va a estar en la db necesariamente si no es favorito...
                            Toast.makeText(getApplicationContext(), "Error3", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                            cargarInfoDesdeDB(pais,getString(R.string.errorInfoPais));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Cargar desde la db
                Toast.makeText(getApplicationContext(), "No se pudo actualizar la informacion", Toast.LENGTH_SHORT).show();
                cargarInfoDesdeDB(pais,getString(R.string.errorInfoPais));
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void cargarInfoDesdeDB(Country pais, String infoError){
        if (pais!=null) {
            TextView totalActivos = (TextView) findViewById(R.id.totalActivos);
            TextView totalConfirmados = (TextView) findViewById(R.id.totalConfirmados);
            TextView totalMuertes = (TextView) findViewById(R.id.totalMuertes);
            TextView nuevosConfirmados = (TextView) findViewById(R.id.nuevosConfirmados);
            TextView nuevosMuertes = (TextView) findViewById(R.id.nuevosMuertes);
            TextView fecha = (TextView) findViewById(R.id.fecha);

            totalActivos.setText(getString(R.string.activosTotales)+ pais.getTotalActivos().toString());
            totalConfirmados.setText(getString(R.string.confirmadosTotales)+pais.getTotalConfirmados().toString());
            totalMuertes.setText(getString(R.string.muertesTotales)+pais.getTotalMuertes().toString());
            nuevosConfirmados.setText(getString(R.string.nuevosConfirmados)+pais.getNuevosConfirmados().toString());
            nuevosMuertes.setText(getString(R.string.nuevosMuertos)+pais.getNuevosMuertes().toString());

            java.sql.Date sqlDate = pais.getDate();
            Date utilDate = new Date(sqlDate.getTime());
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(utilDate);
            Integer year = calendar.get(Calendar.YEAR);
            Integer month = calendar.get(Calendar.MONTH) + 1;
            Integer day = calendar.get(Calendar.DAY_OF_MONTH);
            Integer hour = calendar.get(Calendar.HOUR);
            Integer minute = calendar.get(Calendar.MINUTE);
            fecha.setText(String.format("%d/%d/%d %d:%d", day, month, year, hour, minute));
            drawChart();
        } else {
            // No va a estar en la db necesariamente si no es favorito...
            errorAlCargar(infoError);
        }
    }

    private void errorAlCargar(String notification){
        Toast.makeText(getApplicationContext(), notification, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void noChartData(){
        com.github.mikephil.charting.charts.LineChart layout = findViewById(R.id.chart1);
        ViewGroup.LayoutParams params = layout.getLayoutParams();
        params.height = 150;
        layout.setLayoutParams(params);
    }

    public void drawChart() {
        final RequestQueue queue = Volley.newRequestQueue(this);
        String pais =((TextView)findViewById(R.id.pais)).getText().toString();
        StringRequest strReq = new StringRequest(Request.Method.GET, "https://api.covid19api.com/total/dayone/country/"+pais,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            if (jsonArray.length()>1) {
                                for (int i = 0; i < jsonArray.length(); i++) {
//                                    for (int i = (jsonArray.length() < 200 ? jsonArray.length() : 200); i < jsonArray.length(); i++) {
                                    int value = jsonArray.getJSONObject(i).getInt("Active");
                                    int value2 = jsonArray.getJSONObject(i).getInt("Deaths");
                                    int value3 = jsonArray.getJSONObject(i).getInt("Confirmed");
                                    String date = jsonArray.getJSONObject(i).getString("Date");
//                                        Los datos del dia 2021-03-07T00:00:00Z parecen ser erroneos
//                                        if (!date.equals("2021-03-07T00:00:00Z")) {
                                    date=date.replace("T"," ");
                                    date=date.replace("Z","");
                                    Date utilDate; // = new Date(strDate);
                                    try {
                                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");
                                        utilDate = format.parse(date);
                                    }
                                    catch(ParseException pe) {
                                        throw new IllegalArgumentException(pe);
                                    }

                                    x.add(new Entry(utilDate.getTime(), value));
                                    x2.add(new Entry(utilDate.getTime(), value2));
                                    x3.add(new Entry(utilDate.getTime(), value3));
//                                        }
                                }


                                XAxis xAxis = mChart.getXAxis();
                                // xAxis.setGranularityEnabled(true);
                                // xAxis.setGranularity(100);
                                // xAxis.setLabelCount(450, /*force: */true);
                                xAxis.setLabelRotationAngle(-45);
                                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                                xAxis.setDrawGridLines(false);
                                xAxis.setLabelCount(12, true);
                                xAxis.setValueFormatter(new IAxisValueFormatter() {
                                    @Override
                                    public String getFormattedValue(float value, AxisBase axis) {
                                        Calendar calendar = new GregorianCalendar();
                                        calendar.setTime(new Date((long)value));
                                        int year = calendar.get(Calendar.YEAR);
                                        int month = calendar.get(Calendar.MONTH) + 1;
                                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                                        String aux = day+"/"+month+"/"+year;
                                        return aux;
                                    }
                                });

                                LineDataSet set1 = new LineDataSet(x, "Activos");
                                set1.setLineWidth(1.5f);
                                set1.setCircleRadius(2f);
                                set1.setColor(Color.GREEN);
                                set1.setCircleColor(Color.GREEN);
                                set1.setFillColor(Color.GREEN);
                                set1.setDrawValues(false);
                                LineDataSet set2 = new LineDataSet(x2, "Muertos");
                                set2.setLineWidth(1.5f);
                                set2.setCircleRadius(2f);
                                set2.setColor(Color.RED);
                                set2.setCircleColor(Color.RED);
                                set2.setFillColor(Color.RED);
                                set2.setDrawValues(false);
                                LineDataSet set3 = new LineDataSet(x3, "Confirmados");
                                set3.setLineWidth(1.5f);
                                set3.setCircleRadius(2f);
                                set3.setColor(Color.YELLOW);
                                set3.setCircleColor(Color.YELLOW);
                                set3.setFillColor(Color.YELLOW);
                                set3.setDrawValues(false);

                                ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                                dataSets.add(set1); // add the datasets
                                dataSets.add(set2);
                                dataSets.add(set3);
                                // create a data object with the datasets
                                LineData data = new LineData(dataSets);
                                data.setValueTextColor(Color.WHITE);
                                data.setValueTextSize(9f);
                                mChart.setData(data);
                                mChart.invalidate();
                            } else {
                                noChartData();
                            }
                        } catch (Exception e) {
                            noChartData();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                noChartData();
                Log.e("CountryDetails", "Error: " + error.getMessage());
            }
        });
        queue.add(strReq);
    }

    public void compartir(View view) {
        Intent compartir = new Intent(android.content.Intent.ACTION_SEND);
        compartir.setType("text/plain");
        String mensaje = ((TextView)findViewById(R.id.pais)).getText().toString();
        mensaje = mensaje + "\n"  + ((TextView) findViewById(R.id.fecha)).getText();
        mensaje = mensaje + "\n" + ((TextView) findViewById(R.id.totalActivos)).getText();
        mensaje = mensaje + "\n" + ((TextView) findViewById(R.id.totalConfirmados)).getText();
        mensaje = mensaje + "\n" + ((TextView) findViewById(R.id.totalMuertes)).getText();
        mensaje = mensaje + "\n" + ((TextView) findViewById(R.id.nuevosConfirmados)).getText();
        mensaje = mensaje + "\n" + ((TextView) findViewById(R.id.nuevosMuertes)).getText();
        TextView fecha = (TextView) findViewById(R.id.fecha);
        compartir.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.subjectCompartir)+ ((TextView)findViewById(R.id.pais)).getText().toString());
        compartir.putExtra(android.content.Intent.EXTRA_TEXT, mensaje);
        startActivity(Intent.createChooser(compartir, getString(R.string.tituloCompartir)));
    }

}