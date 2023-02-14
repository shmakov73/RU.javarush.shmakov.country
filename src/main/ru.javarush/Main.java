import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dao.CityDao;
import dao.CountryDao;
import domain.City;
import domain.Country;
import domain.CountryLanguage;
import io.lettuce.core.RedisClient;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import redis.CityCountry;
import redis.Language;
import service.ConnectionSpeedTest;
import service.RedisClientProvider;
import service.SessionFactoryProvider;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);
    private final SessionFactory sessionFactory;
    private final RedisClient redisClient;

    private final RedisClientProvider redisClientProvider;
    private final CityDao cityDao;
    private final CountryDao countryDao;

    public Main(){

        redisClientProvider = new RedisClientProvider();
        Optional<SessionFactory> optionalSessionFactory = new SessionFactoryProvider().getSessionFactory();
        Optional<RedisClient> optionalRedisClient = redisClientProvider.getRedisClient();

        if (optionalSessionFactory.isPresent()){
            sessionFactory = optionalSessionFactory.get();
        } else {
            logger.error("SessionFactory is empty");
            throw new NoSuchElementException("SessionFactory is empty");
        }

        if (optionalRedisClient.isPresent()){
            redisClient = optionalRedisClient.get();
        } else {
            logger.error("RedisClient is empty");
            throw new NoSuchElementException("RedisClient is empty");
        }


            cityDao = new CityDao(sessionFactory);
            countryDao = new CountryDao(sessionFactory);
    }

    private void shutdown() {
            sessionFactory.close();
            logger.info("SessionFactory is closed");
            redisClient.shutdown();
            logger.info("Redis client is closed");
    }

    private List<City> fetchData(Main main) {
        try (Session session = main.sessionFactory.getCurrentSession()) {
            List<City> allCities = new ArrayList<>();
            session.beginTransaction();

            List<Country> countries = main.countryDao.getAll();

            int totalCount = main.cityDao.getTotalCount();
            logger.info("\nTotal cities count " + totalCount);
            int step = 500;
            for (int i = 0; i < totalCount; i += step) {
                allCities.addAll(main.cityDao.getItems(i, step));
            }
            session.getTransaction().commit();
            return allCities;
        }
    }

    public static void main(String[] args) {
        Main main = new Main();
        List<City> allCities = main.fetchData(main);
        List<CityCountry> preparedData = main.transformData(allCities);
        main.redisClientProvider.pushToRedis(preparedData);

        main.sessionFactory.getCurrentSession().close();

        ConnectionSpeedTest test = new ConnectionSpeedTest(main.sessionFactory, main.redisClient);
        test.connectionTest();

        main.shutdown();
    }

    private List<CityCountry> transformData(List<City> cities) {
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
}