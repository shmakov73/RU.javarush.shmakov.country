package ru.javarush.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.javarush.dao.CityHibernateDao;
import ru.javarush.domain.City;
import ru.javarush.domain.CountryLanguage;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import ru.javarush.redis.CityCountry;

import java.util.Random;
import java.util.Set;
import java.util.List;


public class ConnectionSpeedTest {

    private final Integer citiesCountForTest = 10;
    private static final Logger logger = LogManager.getLogger(ConnectionSpeedTest.class);
    private final SessionFactory sessionFactory;
    private final RedisClient redisClient;
    private final CityHibernateDao cityHibernateDao;
    private final ObjectMapper mapper;


    public ConnectionSpeedTest(SessionFactory sessionFactory, RedisClient redisClient) {
        this.sessionFactory = sessionFactory;
        this.redisClient = redisClient;
        cityHibernateDao = new CityHibernateDao(sessionFactory);
        mapper = new ObjectMapper();
    }

    public void connectionTest(){

        List<Integer> ids = getRandomIds(citiesCountForTest);

        long startRedis = System.currentTimeMillis();
        testRedisData(ids);
        long stopRedis = System.currentTimeMillis();

        long startMysql = System.currentTimeMillis();
        testMysqlData(ids);
        long stopMysql = System.currentTimeMillis();

        logger.info("Redis: " + (stopRedis - startRedis) + "ms");
        logger.info("MySQL: " + (stopMysql - startMysql) + "ms");
    }


    private void testRedisData(List<Integer> ids) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            for (Integer id : ids) {
                String value = sync.get(String.valueOf(id));
                try {
                    mapper.readValue(value, CityCountry.class);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    logger.error(e);
                }
            }
        }
    }

    private void testMysqlData(List<Integer> ids) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            for (Integer id : ids) {
                if (cityHibernateDao.getById(id).isPresent()){
                    City city = cityHibernateDao.getById(id).get();
                    Set<CountryLanguage> languages = city.getCountry().getLanguages();
                }

            }
            session.getTransaction().commit();
        }
    }

    private List<Integer> getRandomIds(Integer maxCount){
        return new Random()
                .ints(1, getTotalCitiesCount())
                .limit(maxCount).boxed()
                .toList();
    }

    private Integer getTotalCitiesCount() {
        int totalCount;
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            totalCount = cityHibernateDao.getTotalCount();
            session.getTransaction().commit();
        }
        return totalCount;
    }
}