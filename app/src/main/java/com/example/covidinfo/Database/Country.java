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
    private Long totalActivos;
    @ColumnInfo(name = "totalConfirmados")
    private Long totalConfirmados;
    @ColumnInfo(name = "totalMuertes")
    private Long totalMuertes;
    @ColumnInfo(name = "nuevosConfirmados")
    private Long nuevosConfirmados;
    @ColumnInfo(name = "nuevosMuertes")
    private Long nuevosMuertes;


    @ColumnInfo(name = "date")
    private Date date;
//    @ColumnInfo(name = "date")
//    public String date;

    public Country(){
    }

    public Country(String nameParameter){
        name=nameParameter;
    }

    public Country(String name, Long totalActivos, Long totalConfirmados, Long totalMuertes, Long nuevosConfirmados, Long nuevosMuertes) {
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

    public Long getTotalActivos() {
        return totalActivos;
    }

    public void setTotalActivos(Long totalActivos) {
        this.totalActivos = totalActivos;
    }

    public Long getTotalConfirmados() {
        return totalConfirmados;
    }

    public void setTotalConfirmados(Long totalConfirmados) {
        this.totalConfirmados = totalConfirmados;
    }

    public Long getTotalMuertes() {
        return totalMuertes;
    }

    public void setTotalMuertes(Long totalMuertes) {
        this.totalMuertes = totalMuertes;
    }

    public Long getNuevosConfirmados() {
        return nuevosConfirmados;
    }

    public void setNuevosConfirmados(Long nuevosConfirmados) {
        this.nuevosConfirmados = nuevosConfirmados;
    }

    public Long getNuevosMuertes() {
        return nuevosMuertes;
    }

    public void setNuevosMuertes(Long nuevosMuertes) {
        this.nuevosMuertes = nuevosMuertes;
    }

}
