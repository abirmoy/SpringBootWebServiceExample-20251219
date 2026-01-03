package com.example.ClassRosterWebService.Security;

import com.example.ClassRosterWebService.DAO.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private UserDao userDao;
    
    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }
    
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // TEMPORARILY DISABLE CSRF
            .authorizeHttpRequests(auth -> auth
                // Public access
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**", "/testHash", "/debugUser").permitAll()
                
                // Role-based access with specific URL patterns
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // Teachers - only ADMIN and TEACHER can access
                .requestMatchers("/teachers/**").hasAnyRole("ADMIN", "TEACHER")
                .requestMatchers("/editTeacher").hasAnyRole("ADMIN", "TEACHER")
                .requestMatchers("/updateTeacher").hasAnyRole("ADMIN", "TEACHER")
                .requestMatchers("/addTeacher").hasRole("ADMIN")  // Only admin can add teachers
                .requestMatchers("/deleteTeacher").hasRole("ADMIN")  // Only admin can delete teachers
                
                // Students - different access levels
                .requestMatchers("/students").hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                .requestMatchers("/editStudent").hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                .requestMatchers("/updateStudent").hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                .requestMatchers("/addStudent").hasAnyRole("ADMIN", "TEACHER")  // Students cannot add other students
                .requestMatchers("/deleteStudent").hasAnyRole("ADMIN", "TEACHER")  // Students cannot delete
                .requestMatchers("/enrollStudent").hasAnyRole("ADMIN", "TEACHER")  // Only admin/teacher can enroll
                .requestMatchers("/unenrollStudent").hasAnyRole("ADMIN", "TEACHER")  // Only admin/teacher can unenroll
                
                // Courses - only ADMIN and TEACHER can access
                .requestMatchers("/courses/**").hasAnyRole("ADMIN", "TEACHER")
                .requestMatchers("/courseDetail").hasAnyRole("ADMIN", "TEACHER")
                .requestMatchers("/addCourse").hasAnyRole("ADMIN", "TEACHER")
                .requestMatchers("/editCourse").hasAnyRole("ADMIN", "TEACHER")
                .requestMatchers("/deleteCourse").hasRole("ADMIN")  // Only admin can delete courses
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/access-denied")
            );
        
        return http.build();
    }
    
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
            "/resources/**", 
            "/static/**", 
            "/webjars/**"
        );
    }
}