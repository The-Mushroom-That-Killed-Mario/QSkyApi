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

import javax.ejb.Local;

import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * Доступ до сессии Hibernate.
 *
 * @author egorov
 */
@Local
public interface IHibernateEJBLocal {

    /**
     * Фабрика сессий.
     *
     * @return
     */
    public SessionFactory getSessionFactory();

    /**
     * Открывается новыя сессия. Сессия управляется программно.
     *
     * @return открытая новая сессии для использования
     */
    public Session openSession();

    /**
     * Текущая скессия, вариант использования: одна транзакция - одна сессия.
     * Автоматическое управление.
     *
     * @return
     */
    public Session getCurrentSession();

    /**
     * Текущая скессия, вариант использования: одна транзакция - одна сессия
     * Автоматическое управление.
     * Вариант для коротко записи
     *
     * @return
     */
    public Session cs();

    Exception execute(DoSomething doSome);

    Exception execute(Supplier<? extends Exception> doSome);

    void saveAll(Collection list);

    void saveOrUpdateAll(Collection list);

    void saveOrUpdate(Object obj);

    void deleteAll(Collection list);

    void delete(Object obj);

    List loadAll(Class clazz);

    void load(Object obj, Serializable srlzbl);

    List findByCriteria(DetachedCriteria criteria);

    <T> T find(Class<T> clazz, Serializable srlzbl);

    List find(String hql);

    Session getSession();
}
