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
package com.forgerock.openidm.model;

import com.forgerock.openidm.xml.ns._public.common.common_1.UserType;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

/**
 * End user entity.
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
@Entity
@SecondaryTable(catalog = SimpleDomainObject.DDL_CATALOG, name = User.DDL_TABLE_USER,
pkJoinColumns = {@PrimaryKeyJoinColumn(name = "uuid", referencedColumnName = "uuid")},
uniqueConstraints = @UniqueConstraint(columnNames = {"name"}))
@NamedQueries(value = {
    @NamedQuery(name = User.QUERY_USER_FIND_ALL, query = "SELECT m FROM User m"),
    @NamedQuery(name = User.QUERY_USER_FIND_BY_NAME, query = "SELECT m FROM User m WHERE m.name = ?0")
})
public class User extends SimpleDomainObject<UserType> {

    public static final String code_id = "$Id$";
    public static final String DDL_TABLE_USER = "Users";
    // queries
    public final static String QUERY_USER_FIND_BY_NAME = "User.findByUname";
    public final static String QUERY_USER_FIND_ALL = "User.findAll";
    /**
     *
     */
    private static final long serialVersionUID = -6219139356897428716L;
    private String givenName;
    private String familyName;
    private String fullName;
    private String email;
    private Set<Account> accounts = new HashSet<Account>(0);
    /**
     * The prime value for hash code calculating.
     */
    private static final int PRIME = 31;

    /**
     * The username.
     */
//    private PersonInfo _info = new PersonInfo();
    // private String dataXml;
//    private Map<String, String> _attributes = new HashMap<String, String>(0);
    /**
     * Construct a default user.
     */
    public User() {
    }

    /**
     * Construct a user of the specified domain and usename.
     *
     * @param domain
     *            the domain
     * @param username
     *            the username
     */
    public User(final Domain domain, final String username) {
        //this.domain = domain;
        this.name = username;
    }

    /**
     * Get the username.
     *
     * @return the username
     */
    @Column(table = User.DDL_TABLE_USER, name = "name", unique = true, nullable = false, length = 128)
    public String getName() {
        return name;
    }

    @Column(table = User.DDL_TABLE_USER, name = "familyName", length = 128)
    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    @Column(table = User.DDL_TABLE_USER, name = "fullName", length = 128)
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Column(table = User.DDL_TABLE_USER, name = "givenName", length = 128)
    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    @Column(table = User.DDL_TABLE_USER, name = "email", length = 128)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user",cascade=CascadeType.REMOVE)
    public Set<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<Account> accounts) {
        this.accounts = accounts;
    }

//    @Type(type = "org.forgerock.openidm.hibernate.usertype.XMLType")
//    @Column(name = "xml_data", length = 1073741823)
//    public PersonInfo getInfo() {
//        return _info;
//    }
//
//    public void setInfo(PersonInfo info) {
//        this._info = info;
//    }
//    @CollectionOfElements(fetch = FetchType.EAGER)
//    @JoinTable(name = "PersonAttributes",
//    joinColumns = @JoinColumn(name = "uuid"))
//    @Column(name = "attrvalue", nullable = false)
//    @org.hibernate.annotations.MapKey(columns = {
//        @Column(name = "attrname")
//    })
//    public Map<String, String> getAttributes() {
//        return this._attributes;
//    }
//
//    public void setAttributes(
//            Map<String, String> resourceAttributeses) {
//        this._attributes = resourceAttributeses;
//    }
    /**
     * Get the identifier.
     *
     * @return the identifier
     */
    @Transient
    public String getIdentifier() {
        return String.format("%1$s%2$s%3$s", getDomain().getIdentifierPrefix(), getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = PRIME * result + ((getDomain() == null) ? 0 : getDomain().hashCode());
        result = PRIME * result + ((getName() == null) ? 0 : getName().hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof User)) {
            return false;
        }
        User other = (User) obj;
        if (getDomain() == null) {
            if (other.getDomain() != null) {
                return false;
            }
        } else if (!getDomain().equals(other.getDomain())) {
            return false;
        }
        if (getName() == null) {
            if (other.getName() != null) {
                return false;
            }
        } else if (!getName().equals(other.getName())) {
            return false;
        }
        return true;
    }
}
