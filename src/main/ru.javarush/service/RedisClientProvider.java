package service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.CityCountry;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class RedisClientProvider {

    private static final Logger logger = LogManager.getLogger(RedisClientProvider.class);
    private RedisClient redisClient;
    private final ObjectMapper mapper;
    public RedisClientProvider() {

        try {
            redisClientInit();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            logger.info("\nConnected to Redis\n");
        }
        mapper = new ObjectMapper();
    }

    public Optional<RedisClient> getRedisClient() {
        return Optional.ofNullable(redisClient);
    }

    public void pushToRedis(List<CityCountry> data) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            for (CityCountry cityCountry : data) {
                try {
                    sync.set(String.valueOf(cityCountry.getId()), mapper.writeValueAsString(cityCountry));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    logger.error(e);
                }
            }

        }
    }

    private void redisClientInit() throws FileNotFoundException {
        FileInputStream fis;
        Properties property = new Properties();

        try {
            fis = new FileInputStream("src/main/resources/redisConnect.properties");
            property.load(fis);

            String host = property.getProperty("host");
            int port = Integer.parseInt(property.getProperty("port"));
            redisClient = RedisClient.create(RedisURI.create(host, port));

        } catch (IOException  e) {
            logger.error("property file is missing");
        }
    }

}
