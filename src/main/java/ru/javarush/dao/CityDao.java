package ru.javarush.dao;

import ru.javarush.domain.City;

import java.util.List;
import java.util.Optional;

public interface CityDao {

    Optional<List<City>> getItems(int offset, int limit);

    int getTotalCount();

    Optional<City> getById(Integer id);
}