package com.example.covidinfo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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

public class CountryDetails extends AppCompatActivity {

    ArrayList<Entry> x;
    ArrayList<Entry> x2;
    ArrayList<String> y;
    private LineChart mChart;
    public String TAG = "CountryDetails";

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

        x = new ArrayList<Entry>();
        x2 = new ArrayList<Entry>();
        y = new ArrayList<String>();
        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setDrawGridBackground(false);
//        mChart.setDescription("Covid");
        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setPinchZoom(true);
//        MarkerView mv = new MarkerView();
//        // set the marker to the chart
//        mChart.setMarkerView(mv);
        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setAvoidFirstLastClipping(true);
        xl.setDrawAxisLine(true);
        xl.setDrawLabels(false);
        YAxis leftAxis = mChart.getAxisLeft();
//        leftAxis.setStartAtZero(true);
        leftAxis.setAxisMinimum(0);
        leftAxis.setTextColor(Color.WHITE);
        //        leftAxis.setInverted(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);
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
                            drawChart();
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
                if(pais!=null) {
                    cargarInfoDesdeDB(pais);
                } else {
                    // No va a estar en la db necesariamente si no es favorito...
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
        compartir.putExtra(android.content.Intent.EXTRA_SUBJECT, "Info actual del Covid19 en "+ ((TextView)findViewById(R.id.pais)).getText().toString());
        compartir.putExtra(android.content.Intent.EXTRA_TEXT, mensaje);
        startActivity(Intent.createChooser(compartir, "Compartir vía"));
    }

    public void drawChart() {
//        private void drawChart() {
            final RequestQueue queue = Volley.newRequestQueue(this);

//            String tag_string_req = "req_chart";
            String pais =((TextView)findViewById(R.id.pais)).getText().toString();
            StringRequest strReq = new StringRequest(Request.Method.GET, "https://api.covid19api.com/total/dayone/country/"+pais,
                    new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
//                            Log.d(TAG, "Response: " + response);
                            try {
                                JSONArray jsonArray = new JSONArray(response);
                                if (jsonArray.length()>1) {
//                                for (int i = 0; i < jsonArray.length(); i++) {
                                    for (int i = (jsonArray.length() < 200 ? jsonArray.length() : 200); i < jsonArray.length(); i++) {
                                        int value = jsonArray.getJSONObject(i).getInt("Active");
                                        int value2 = jsonArray.getJSONObject(i).getInt("Deaths");
                                        String date = jsonArray.getJSONObject(i).getString("Date");
                                        if (!date.equals("2021-03-07T00:00:00Z")) {
//                                        date=date.replace("T"," ");
//                                        date=date.replace("Z","");
//                                        Date utilDate; // = new Date(strDate);
//                                        try {
//                                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");
//                                            utilDate = format.parse(date);
//                                        }
//                                        catch(ParseException pe) {
//                                            throw new IllegalArgumentException(pe);
//                                        }
//                                        Calendar calendar = new GregorianCalendar();
//                                        calendar.setTime(utilDate);
//                                        int year = calendar.get(Calendar.YEAR);
//                                        int month = calendar.get(Calendar.MONTH) + 1;
//                                        int day = calendar.get(Calendar.DAY_OF_MONTH);
//                                        String fecha = day +"/"+month +"/"+year;
//                                        // new Entry() invierte los parametros en la version 2
//                                        // v2.2.3 -> new Entry(value,i)
//                                        // x.add(new Entry(value,i));
                                            x.add(new Entry(i, value));
                                            x2.add(new Entry(i, value2));
//                                        y.add(fecha);
                                        }
                                    }

//                                final ArrayList<String> xLabel = new ArrayList<>();
//                                xLabel.add(y.get(0));
//                                xLabel.add(y.get(y.size()-1));
//                                XAxis xAxis = mChart.getXAxis();
//                                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//                                xAxis.setDrawGridLines(false);
//                                xAxis.setLabelCount(12, true);
//                                xAxis.setValueFormatter(new IAxisValueFormatter() {
//                                    @Override
//                                    public String getFormattedValue(float value, AxisBase axis) {
//                                        return xLabel.get((int)value);
//                                    }
//                                });

                                    LineDataSet set1 = new LineDataSet(x, "Casos activos");
                                    set1.setLineWidth(2f);
                                    set1.setCircleRadius(2.5f);
                                    set1.setColor(Color.GREEN);
                                    set1.setCircleColor(Color.GREEN);
                                    set1.setFillColor(Color.GREEN);
                                    set1.setDrawValues(false);
                                    LineDataSet set2 = new LineDataSet(x2, "Cantidad muertos");
                                    set2.setLineWidth(2f);
                                    set2.setCircleRadius(2.5f);
                                    set2.setColor(Color.RED);
                                    set2.setCircleColor(Color.RED);
                                    set2.setFillColor(Color.RED);
                                    set2.setDrawValues(false);

                                    ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                                    dataSets.add(set1); // add the datasets
                                    dataSets.add(set2);

                                    // create a data object with the datasets
                                    LineData data = new LineData(dataSets);
                                    data.setValueTextColor(Color.WHITE);
                                    data.setValueTextSize(9f);

//                                mChart.highlightValue(null);
                                    // set data
                                    mChart.setData(data);
                                    mChart.invalidate();

//                                LineData data = new LineData(y,set1);
//                                mChart.setData(data);
//                                mChart.invalidate();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Error: " + error.getMessage());
                }
            });
//            strReq.setRetryPolicy(new RetryPolicy() {
//
//                @Override
//                public void retry(VolleyError arg0) throws VolleyError {
//                }
//
//                @Override
//                public int getCurrentTimeout() {
//                    return 0;
//                }
//
//                @Override
//                public int getCurrentRetryCount() {
//                    return 0;
//                }
//            });
//            strReq.setShouldCache(false);
            queue.add(strReq);
        }

}