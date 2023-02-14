package service;

import domain.City;
import domain.Country;
import domain.CountryLanguage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.Optional;

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
        logger.info("\nConnected to MySQl database\n");
    }

    public Optional<SessionFactory> getSessionFactory() {
        return Optional.ofNullable(sessionFactory);
    }
}
