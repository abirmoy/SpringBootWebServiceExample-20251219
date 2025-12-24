package com.example.ClassRosterWebService.Security;

import com.example.ClassRosterWebService.DAO.UserDao;
import com.example.ClassRosterWebService.Entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    
    @Autowired
    private UserDao userDao;
    
    public UserDetailsServiceImpl() {
        logger.info("UserDetailsServiceImpl initialized");
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Attempting to load user: {}", username);
        
        User user = userDao.getUserByUsername(username);
        
        if (user == null) {
            logger.error("User not found in database: {}", username);
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        
        logger.info("Found user: {} (enabled: {})", username, user.isEnabled());
        logger.info("User password hash: {}", user.getPassword());
        
        if (!user.isEnabled()) {
            logger.error("User account is disabled: {}", username);
            throw new UsernameNotFoundException("User account is disabled: " + username);
        }
        
        List<String> roles = userDao.getRolesForUser(username);
        logger.info("User roles: {}", roles);
        
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            getAuthorities(roles)
        );
    }
    
    private Collection<? extends GrantedAuthority> getAuthorities(List<String> roles) {
        return roles.stream()
            .map(roleName -> "ROLE_" + roleName)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }
}