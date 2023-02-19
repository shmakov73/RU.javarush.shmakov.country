package ru.javarush.dao;

import ru.javarush.domain.Country;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;

public class CountryHibernateDao implements CountryDao {
    private final SessionFactory sessionFactory;

    public CountryHibernateDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public List<Country> getAll() {
        Query<Country> query = sessionFactory
                .getCurrentSession()
                .createQuery("select c from Country c join fetch c.languages", Country.class);
        return query.list();
    }
}