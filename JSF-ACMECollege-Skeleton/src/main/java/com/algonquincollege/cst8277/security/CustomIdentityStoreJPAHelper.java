/********************************************************************************************************
 * File: CustomIdentityStoreJPAHelper.java Course Materials CST 8277
 */
package com.algonquincollege.cst8277.security;

import static com.algonquincollege.cst8277.utility.MyConstants.PARAM1;
import static com.algonquincollege.cst8277.utility.MyConstants.PU_NAME;
import static java.util.Collections.emptySet;

import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.algonquincollege.cst8277.entity.SecurityRole;
import com.algonquincollege.cst8277.entity.SecurityUser;

import jakarta.ejb.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Singleton
public class CustomIdentityStoreJPAHelper {
    private static final Logger LOG = LogManager.getLogger();

    @PersistenceContext(name = PU_NAME)
    protected EntityManager em;

    public SecurityUser findUserByName(String username) {
        LOG.debug("find a SecurityUser by name = {}", username);
        try {
            return em.createNamedQuery(SecurityUser.SECURITY_USER_BY_NAME, SecurityUser.class)
                .setParameter(PARAM1, username)
                .getSingleResult();
        }
        catch (NoResultException e) {
            return null;
        }
    }

    public Set<String> findRoleNamesForUser(String username) {
        LOG.debug("find Roles For Username={}", username);
        SecurityUser securityUser = findUserByName(username);
        if (securityUser == null) {
            return emptySet();
        }
        return securityUser.getRoles().stream().map(SecurityRole::getRoleName).collect(Collectors.toSet());
    }

    @Transactional
    public void saveSecurityUser(SecurityUser user) { em.persist(user); }

    @Transactional
    public void saveSecurityRole(SecurityRole role) { em.persist(role); }
}
