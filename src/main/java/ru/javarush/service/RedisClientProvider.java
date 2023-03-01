package ru.javarush.service;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.FileNotFoundException;

public class RedisClientProvider {

    private static final Logger logger = LogManager.getLogger(RedisClientProvider.class);
    private RedisClient redisClient;

    public RedisClientProvider() {

        try {
            redisClientInit();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            logger.info("\nRedisClient is ready\n");
        }
    }

    public RedisClient getRedisClient() {
        return redisClient;
    }


    private void redisClientInit() throws FileNotFoundException {
        RedisClientPropertyReader propertyReader = new RedisClientPropertyReader();
        redisClient = RedisClient.create(RedisURI.create(propertyReader.getHost(), propertyReader.getPort()));

    }
}