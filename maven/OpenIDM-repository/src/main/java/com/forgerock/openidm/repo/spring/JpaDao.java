/*
 *
 * Copyright (c) 2010 ForgeRock Inc. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1.php or
 * OpenIDM/legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at OpenIDM/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted 2010 [name of copyright owner]"
 *
 * $Id$
 */
package com.forgerock.openidm.repo.spring;

import com.forgerock.openidm.model.Property;
import com.forgerock.openidm.model.SimpleDomainObject;
import com.forgerock.openidm.repo.PagingRepositoryType;
import com.forgerock.openidm.xml.ns._public.common.common_1.PagingType;
import com.forgerock.openidm.xml.schema.XPathSegment;
import com.forgerock.openidm.xml.schema.XPathType;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.springframework.orm.jpa.JpaCallback;
import org.springframework.orm.jpa.support.JpaDaoSupport;
import org.springframework.transaction.annotation.Transactional;

/**
 * TODO
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
@Transactional
public abstract class JpaDao<K, E extends SimpleDomainObject> extends JpaDaoSupport {

    protected Class<E> entityClass;

    public JpaDao() {
        ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        this.entityClass = (Class<E>) genericSuperclass.getActualTypeArguments()[1];
    }

    public void persist(E entity) {
        getJpaTemplate().persist(entity);
    }

    public void remove(E entity) {
        //TODO: tidy up
        //entity = getJpaTemplate().merge(entity);

        getJpaTemplate().remove(getJpaTemplate().getReference(entity.getClass(), entity.getOid()));
    }

    public E merge(E entity) {
        return getJpaTemplate().merge(entity);
    }

    public void refresh(E entity) {
        getJpaTemplate().refresh(entity);
    }

    public E findById(K id) {
        //TODO: tidy up
        E entity = getJpaTemplate().find(entityClass, id);

        //TODO: ugly hack to load all properties
        if (null != entity) {
            if (null != entity.getProperties()) {
                for (Property property : entity.getProperties()) {
                    property.getPropertyName();
                    property.getPropertyValue();
                }
            }
        }
        return entity;
    }

    public E flush(E entity) {
        getJpaTemplate().flush();
        return entity;
    }

    public List<E> findAllOfType(final String dtype) {
        Object res = getJpaTemplate().execute(new JpaCallback() {

            public Object doInJpa(EntityManager em) throws PersistenceException {
                Query q = em.createQuery("SELECT h FROM " +
                        entityClass.getName() + " h where dtype = '" + dtype + "'");

                List<E> entities = (List<E>) q.getResultList();
                for (E entity : entities) {
                    if (null != entity) {
                        if (null != entity.getProperties()) {
                            for (Property property : entity.getProperties()) {
                                property.getPropertyName();
                                property.getPropertyValue();
                            }
                        }
                    }
                }


                return entities;
            }
        });

        return (List<E>) res;
    }

    public List<E> findRangeUsers(final String dtype, final PagingRepositoryType paging) {
        Object res = getJpaTemplate().execute(new JpaCallback() {

            public Object doInJpa(EntityManager em) throws PersistenceException {

                if (paging.getOffset() == -1) {
                    Query g = em.createQuery("SELECT count(*) FROM " +
                            entityClass.getName() + " h where dtype = '" + dtype + "'");
                    List resultSet = g.getResultList();
                    if (resultSet != null && resultSet.size() > 0) {
                        Long recordCount = (Long) resultSet.get(0);
                        paging.setOffset(recordCount.intValue() - (recordCount.intValue() % paging.getMaxSize()));
                    }


                    System.out.println("offset " + paging.getOffset());


                }

                Query q = em.createQuery("SELECT h FROM " +
                        entityClass.getName() + " h where dtype = '" + dtype + "' ORDER BY " + paging.getOrderBy() + " " + paging.getOrderDirection());
                q.setFirstResult(paging.getOffset());
                q.setMaxResults(paging.getMaxSize());

                List<E> entities = (List<E>) q.getResultList();
                for (E entity : entities) {
                    if (null != entity) {
                        if (null != entity.getProperties()) {
                            for (Property property : entity.getProperties()) {
                                property.getPropertyName();
                                property.getPropertyValue();
                            }
                        }
                    }
                }


                return entities;
            }
        });

        return (List<E>) res;

    }

    public List<E> findAll() {
        Object res = getJpaTemplate().execute(new JpaCallback() {

            public Object doInJpa(EntityManager em) throws PersistenceException {
                Query q = em.createQuery("SELECT h FROM " +
                        entityClass.getName() + " h");
                return q.getResultList();
            }
        });

        return (List<E>) res;
    }

    public Integer removeAll() {
        return (Integer) getJpaTemplate().execute(new JpaCallback() {

            public Object doInJpa(EntityManager em) throws PersistenceException {
                Query q = em.createQuery("DELETE FROM " +
                        entityClass.getName() + " h");
                return q.executeUpdate();
            }
        });
    }

    public Object execute(final String query) {
        return (Object) getJpaTemplate().execute(new JpaCallback() {

            public Object doInJpa(EntityManager em) throws PersistenceException {
                Query q = em.createQuery(query);
                return q.getResultList();
            }
        });
    }

//    public void remove(final String oid) {
//        getJpaTemplate().execute(new JpaCallback() {
//
//            public Object doInJpa(EntityManager em) throws PersistenceException {
//                Query q = em.createQuery("DELETE FROM " +
//                        entityClass.getName() + " h where h.oid = '" + oid + "'");
//                return q.executeUpdate();
//            }
//        });
//    }
}
