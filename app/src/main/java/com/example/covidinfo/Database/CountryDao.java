package com.example.covidinfo.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CountryDao {
    @Query("SELECT * FROM country")
    List<Country> getAll();

    @Query("SELECT * FROM country WHERE name==:name LIMIT 1")
    Country findByName(String name);

    @Insert
    void insert(Country country);

    @Insert
    void insertAll(Country... countries);

    @Query("DELETE FROM country")
    public void nukeTable();

    @Delete
    void delete(Country country);
}