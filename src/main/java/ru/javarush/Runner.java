package ru.javarush;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import ru.javarush.dao.CityHibernateDao;
import ru.javarush.dao.CountryHibernateDao;
import ru.javarush.domain.City;
import ru.javarush.redis.CityCountry;
import ru.javarush.service.ConnectionSpeedTest;
import ru.javarush.service.DataPreparing;
import ru.javarush.service.RedisClientProvider;
import ru.javarush.service.SessionFactoryProvider;

import java.util.List;

public class Runner {

    private static final Logger logger = LogManager.getLogger(DataPreparing.class);

    private final DataPreparing dataPreparing;

    private final ObjectMapper mapper;

    private final SessionFactory sessionFactory;

    private final RedisClient redisClient;

    private final CityHibernateDao cityHibernateDao;

    private final CountryHibernateDao countryHibernateDao;

    public Runner(){

        sessionFactory = new SessionFactoryProvider().getSessionFactory();
        redisClient = new RedisClientProvider().getRedisClient();

        dataPreparing = new DataPreparing(this.sessionFactory, redisClient);
        mapper = new ObjectMapper();

        cityHibernateDao = new CityHibernateDao(this.sessionFactory);
        countryHibernateDao = new CountryHibernateDao(this.sessionFactory);

    }

    public static void main(String[] args) {
        Runner runner = new Runner();
        List<City> cities = runner.dataPreparing.fetchData(runner.countryHibernateDao, runner.cityHibernateDao);
        List<CityCountry> cityCountries = runner.dataPreparing.transformData(cities);
        runner.dataPreparing.pushToRedis(cityCountries, runner.mapper);

        runner.sessionFactory.getCurrentSession().close();

        ConnectionSpeedTest test = new ConnectionSpeedTest(runner.sessionFactory, runner.redisClient);
        test.connectionTest();
        runner.shutdown();
    }

    private void shutdown() {
        sessionFactory.close();
        logger.info("SessionFactory is closed");
        redisClient.shutdown();
        logger.info("Redis client is closed");
    }
}