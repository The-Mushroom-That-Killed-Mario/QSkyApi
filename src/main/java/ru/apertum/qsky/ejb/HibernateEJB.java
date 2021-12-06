/*
 *  Copyright (C) 2010 {Apertum}Projects. web: www.apertum.ru email: info@apertum.ru
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ru.apertum.qsky.ejb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import ru.apertum.qsky.common.ServerException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author egorov
 */
@Singleton(mappedName = "ejb/qskyapi/hibernate_session_factory", name = "qskyapi/HibernateEJB")
public class HibernateEJB implements IHibernateEJBLocal {
    private Logger log = LogManager.getLogger(HibernateEJB.class);

    private SessionFactory sessionFactory = null;
    private StandardServiceRegistry registry = null;

    @PostConstruct
    private void buildHibernateSessionFactory() {
        sessionFactory = getFactory();
    }

    private SessionFactory getFactory() {
        if (sessionFactory == null) {
            log.info("Create hibernate session factiry.");
            try {
                // Create registry
                registry = new StandardServiceRegistryBuilder().configure().build();
                // Create MetadataSources
                MetadataSources sources = new MetadataSources(registry);
                // Create Metadata
                Metadata metadata = sources.getMetadataBuilder().build();
                // Create SessionFactory
                sessionFactory = metadata.getSessionFactoryBuilder().build();
            } catch (Exception e) {
                log.error("Error to create hibernate session factiry", e);
                if (registry != null) {
                    StandardServiceRegistryBuilder.destroy(registry);
                }
                throw new ServerException(e);
            }
        }
        return sessionFactory;
    }

    private void shutdown() {
        if (registry != null) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    @PreDestroy
    private void closeHibernateSessionFactory() {
        log.info("Shutdown hibernate session factiry.");
        sessionFactory.close();
        shutdown();
    }

    @Override
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Override
    public Session openSession() {
        return sessionFactory.openSession();
    }

    @Override
    public Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public Session cs() {
        return getCurrentSession();
    }

    /**
     * Для транзакций через передачу на выполнениее класса с методом где реализована работа с БД.
     *
     * @param doSome функция, возвращающая без возврата исключение, в ней работа с БД.
     * @return произошедшее исключение.
     */
    @Override
    public Exception execute(DoSomething doSome) {
        return execute(() -> {
            try {
                doSome.go();
            } catch (Exception ex) {
                return ex;
            }
            return null;
        });
    }

    /**
     * Для транзакций через передачу на выполнениее класса с методом где реализована работа с БД.
     *
     * @param doSome функция, возвращающая исключение, в ней работа с БД.
     * @return произошедшее исключение.
     */
    @Override
    public Exception execute(Supplier<? extends Exception> doSome) {
        final Session session = getSession();
        final Transaction transaction = session.beginTransaction();
        try {
            Exception exception = doSome.get();
            if (exception != null) {
                log.error("BD has problem.", exception);
                transaction.markRollbackOnly();
            }
            return exception;
        } catch (Exception ex) {
            log.error("BD has problem!", ex);
            transaction.markRollbackOnly();
            return ex;
        } finally {
            if (transaction.getRollbackOnly()) {
                log.error("Transaction Rollback!");
                transaction.rollback();
            } else {
                transaction.commit();
            }
            session.close();
        }
    }

    /**
     * Сохраним всех. Без обновления.
     *
     * @param list их всех в БД.
     */
    @Override
    public void saveAll(Collection list) {
        final Session ses = getSession();
        list.forEach(ses::save);
        ses.flush();
    }

    /**
     * Сохраним или обновим всех.
     *
     * @param list их всех в БД.
     */
    @Override
    public void saveOrUpdateAll(Collection list) {
        final Session ses = getSession();
        list.forEach(ses::saveOrUpdate);
        ses.flush();
    }

    /**
     * Сохраним или обновим объект в БД.
     *
     * @param obj его сохраним.
     */
    @Override
    public void saveOrUpdate(Object obj) {
        final Session ses = getSession();
        ses.saveOrUpdate(obj);
        ses.flush();
    }

    /**
     * Удалить коллекцию из БД.
     *
     * @param list их всех удалим.
     */
    @Override
    public void deleteAll(Collection list) {
        final Session ses = getSession();
        list.forEach(ses::delete);
        ses.flush();
    }

    /**
     * Просто удалить объект из БД.
     *
     * @param obj его удалим.
     */
    @Override
    public void delete(Object obj) {
        final Session ses = getSession();
        ses.delete(obj);
        ses.flush();
    }

    @Override
    public List loadAll(Class clazz) {
        return findByCriteria(DetachedCriteria.forClass(clazz).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY));
    }

    /**
     * Найти единственную запись и проиницыализировать готовый объект.
     *
     * @param obj    Заполняем этот объект.
     * @param srlzbl ищим по этому ID.
     */
    @Override
    public void load(Object obj, Serializable srlzbl) {
        try (Session ses = openSession()) {
            ses.load(obj, srlzbl);
        }
    }

    /**
     * Найти по критерию.
     *
     * @param criteria критерий.
     * @return Найденный список.
     */
    @Override
    public List findByCriteria(DetachedCriteria criteria) {
        try (Session ses = openSession()) {
            return criteria.getExecutableCriteria(ses).list();
        }
    }

    /**
     * Найти единственную запись.
     *
     * @param clazz  ищим этот тип.
     * @param srlzbl ищим по этому ID.
     * @param <T>    Результирующий тип.
     * @return Объект соглано ID.
     */
    @Override
    public <T> T find(Class<T> clazz, Serializable srlzbl) {
        try (Session ses = openSession()) {
            return ses.get(clazz, srlzbl);
        }
    }

    /**
     * Выполнить поиск. Выражение hql.
     *
     * @param hql Выражение hql.
     * @return Список найденных записей как объекты.
     */
    @Override
    public List find(String hql) {
        try (Session ses = openSession()) {
            return ses.createQuery(hql).list();
        }
    }

    /**
     * Получить открытую сессию для работы с БД.
     *
     * @return готовая хиберовская сессия.
     */
    @Override
    public Session getSession() {
        final Session currentSession = sessionFactory.getCurrentSession();
        if (currentSession != null && currentSession.isOpen()) {
            return currentSession;
        } else {
            return sessionFactory.openSession();
        }
    }
}

