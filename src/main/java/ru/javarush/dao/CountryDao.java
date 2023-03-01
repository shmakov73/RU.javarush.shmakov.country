package ru.javarush.dao;

import ru.javarush.domain.Country;

import java.util.List;

public interface CountryDao {

    List<Country> getAll();

}