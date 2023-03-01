package ru.javarush.service;

import ru.javarush.domain.City;
import ru.javarush.domain.Country;
import ru.javarush.domain.CountryLanguage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class SessionFactoryProvider {

    private static final Logger logger = LogManager.getLogger(SessionFactoryProvider.class);
    private final SessionFactory sessionFactory;
    public SessionFactoryProvider() {
        sessionFactory = new Configuration()
                .configure()
                .addAnnotatedClass(City.class)
                .addAnnotatedClass(Country.class)
                .addAnnotatedClass(CountryLanguage.class)
                .buildSessionFactory();
        logger.info("\nSessionFactory is ready\n");
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}