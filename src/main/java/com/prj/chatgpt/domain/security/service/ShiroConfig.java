package com.prj.chatgpt.domain.security.service;

import com.prj.chatgpt.domain.security.service.realm.JwtRealm;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SubjectFactory;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.web.filter.authc.AnonymousFilter;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.DefaultWebSubjectFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration class for setting up Apache Shiro with JWT (JSON Web Token) integration.
 */
@Configuration
public class ShiroConfig {

    /**
     * Creates a custom SubjectFactory that prevents session creation.
     * @return SubjectFactory - a custom SubjectFactory for JWT
     */
    @Bean
    public SubjectFactory subjectFactory() {
        class JwtDefaultSubjectFactory extends DefaultWebSubjectFactory {
            @Override
            public Subject createSubject(SubjectContext context) {
                context.setSessionCreationEnabled(false);
                return super.createSubject(context);
            }
        }
        return new JwtDefaultSubjectFactory();
    }

    /**
     * Creates a Realm for JWT authentication.
     * @return Realm - a JWT-specific Realm
     */
    @Bean
    public Realm realm() {
        return new JwtRealm();
    }

    /**
     * Configures and returns a DefaultWebSecurityManager for Shiro.
     * @return DefaultWebSecurityManager - the configured security manager
     */
    @Bean
    public DefaultWebSecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(realm());
        // Close ShiroDAO function
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        // We don't need to store Shiro session in anywhere(including Http Session)
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        securityManager.setSubjectDAO(subjectDAO);
        /**
         * The setSubjectFactory method is called with subjectFactory(),
         * which can ban the getSession method of Subject.
         */
        securityManager.setSubjectFactory(subjectFactory());
        return securityManager;
    }

    /**
     * Configures and returns a ShiroFilterFactoryBean with JWT filter and other necessary filters.
     * @return ShiroFilterFactoryBean - the configured filter factory bean
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean() {
        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        shiroFilter.setSecurityManager(securityManager());
        shiroFilter.setLoginUrl("/unauthenticated");
        shiroFilter.setUnauthorizedUrl("/unauthorized");
        // Add a jwt filter
        Map<String, Filter> filterMap = new HashMap<>();
        // Setting the filer [anon\logout can do not set]
        filterMap.put("anon", new AnonymousFilter());
        filterMap.put("jwt", new JwtFilter());
        filterMap.put("logout", new LogoutFilter());
        shiroFilter.setFilters(filterMap);

        /**
         * Interceptor, specify which interceptor the method should use:
         * [login->anon][logout->logout][verify->jwt]
         */
        Map<String, String> filterRuleMap = new LinkedHashMap<>();
        filterRuleMap.put("/login", "anon");
        filterRuleMap.put("/logout", "logout");
        filterRuleMap.put("/verify", "jwt");
        shiroFilter.setFilterChainDefinitionMap(filterRuleMap);
        return shiroFilter;
    }
}
