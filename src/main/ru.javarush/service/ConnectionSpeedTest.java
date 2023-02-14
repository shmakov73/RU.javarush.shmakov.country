package service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dao.CityDao;
import domain.City;
import domain.CountryLanguage;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import redis.CityCountry;

import java.util.Random;
import java.util.Set;
import java.util.List;


public class ConnectionSpeedTest {

    private Integer citiesCountForTest = 5;
    private static final Logger logger = LogManager.getLogger(ConnectionSpeedTest.class);
    private final SessionFactory sessionFactory;
    private final RedisClient redisClient;
    private final CityDao cityDao;
    private final ObjectMapper mapper;


    public ConnectionSpeedTest(SessionFactory sessionFactory, RedisClient redisClient) {
        this.sessionFactory = sessionFactory;
        this.redisClient = redisClient;
        cityDao = new CityDao(sessionFactory);
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
                City city = cityDao.getById(id);
                Set<CountryLanguage> languages = city.getCountry().getLanguages();
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
            totalCount = cityDao.getTotalCount();
            session.getTransaction().commit();
        }
        return totalCount;
    }
}
