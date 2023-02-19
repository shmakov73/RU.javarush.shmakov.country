package ru.javarush.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import ru.javarush.dao.CountryHibernateDao;
import ru.javarush.domain.City;
import ru.javarush.domain.Country;
import ru.javarush.dao.CityHibernateDao;
import ru.javarush.domain.CountryLanguage;
import ru.javarush.redis.CityCountry;
import ru.javarush.redis.Language;

import java.util.*;
import java.util.stream.Collectors;

public class DataPreparing {

    private static final Logger logger = LogManager.getLogger(DataPreparing.class);

    private final SessionFactory sessionFactory;

    private final RedisClient redisClient;

    public DataPreparing(SessionFactory sessionFactory, RedisClient redisClient){
        this.redisClient = redisClient;
        this.sessionFactory = sessionFactory;
    }

    public List<City> fetchData(CountryHibernateDao countryHibernateDao, CityHibernateDao cityHibernateDao) {
        try (Session session = sessionFactory.getCurrentSession()) {
            List<City> allCities = new ArrayList<>();
            session.beginTransaction();

            List<Country> countries = countryHibernateDao.getAll();

            int totalCount = cityHibernateDao.getTotalCount();
            int step = 500;
            for (int i = 0; i < totalCount; i += step) {
                Optional<List<City>> items = cityHibernateDao.getItems(i, step);
                if (items.isPresent()){
                    List<City> cities = items.get();
                    allCities.addAll(cities);
                } else {
                    logger.error("SessionFactory is empty");
                    throw new NoSuchElementException("List<Cities> is empty");
                }
            }
            session.getTransaction().commit();
            return allCities;
        }
    }

    public List<CityCountry> transformData(List<City> cities) {
        return cities.stream().map(city -> {
            CityCountry res = new CityCountry();
            res.setId(city.getId());
            res.setName(city.getName());
            res.setPopulation(city.getPopulation());
            res.setDistrict(city.getDistrict());

            Country country = city.getCountry();
            res.setAlternativeCountryCode(country.getAlternativeCode());
            res.setContinent(country.getContinent());
            res.setCountryCode(country.getCode());
            res.setCountryName(country.getName());
            res.setCountryPopulation(country.getPopulation());
            res.setCountryRegion(country.getRegion());
            res.setCountrySurfaceArea(country.getSurfaceArea());
            Set<CountryLanguage> countryLanguages = country.getLanguages();
            Set<Language> languages = countryLanguages.stream().map(cl -> {
                Language language = new Language();
                language.setLanguage(cl.getLanguage());
                language.setOfficial(cl.getOfficial());
                language.setPercentage(cl.getPercentage());
                return language;
            }).collect(Collectors.toSet());
            res.setLanguages(languages);

            return res;
        }).collect(Collectors.toList());
    }

    public void pushToRedis(List<CityCountry> data, ObjectMapper mapper) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            for (CityCountry cityCountry : data) {
                try {
                    sync.set(String.valueOf(cityCountry.getId()), mapper.writeValueAsString(cityCountry));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    logger.error(e);
                    throw new RuntimeException(e);
                }
            }

        }
    }
}