package com.example.covidinfo.Database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Date;


@Entity
public class Country {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "totalActivos")
    private String totalActivos;
    @ColumnInfo(name = "totalConfirmados")
    private String totalConfirmados;
    @ColumnInfo(name = "totalMuertes")
    private String totalMuertes;
    @ColumnInfo(name = "nuevosConfirmados")
    private String nuevosConfirmados;
    @ColumnInfo(name = "nuevosMuertes")
    private String nuevosMuertes;


    @ColumnInfo(name = "date")
    private Date date;
//    @ColumnInfo(name = "date")
//    public String date;

    public Country(){
    }

    public Country(String nameParameter){
        name=nameParameter;
    }

    public Country(String name, String totalActivos, String totalConfirmados, String totalMuertes, String nuevosConfirmados, String nuevosMuertes) {
        this.name = name;
        this.totalActivos = totalActivos;
        this.totalConfirmados = totalConfirmados;
        this.totalMuertes = totalMuertes;
        this.nuevosConfirmados = nuevosConfirmados;
        this.nuevosMuertes = nuevosMuertes;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public String getTotalActivos() {
        return totalActivos;
    }

    public void setTotalActivos(String totalActivos) {
        this.totalActivos = totalActivos;
    }

    public String getTotalConfirmados() {
        return totalConfirmados;
    }

    public void setTotalConfirmados(String totalConfirmados) {
        this.totalConfirmados = totalConfirmados;
    }

    public String getTotalMuertes() {
        return totalMuertes;
    }

    public void setTotalMuertes(String totalMuertes) {
        this.totalMuertes = totalMuertes;
    }

    public String getNuevosConfirmados() {
        return nuevosConfirmados;
    }

    public void setNuevosConfirmados(String nuevosConfirmados) {
        this.nuevosConfirmados = nuevosConfirmados;
    }

    public String getNuevosMuertes() {
        return nuevosMuertes;
    }

    public void setNuevosMuertes(String nuevosMuertes) {
        this.nuevosMuertes = nuevosMuertes;
    }

}
