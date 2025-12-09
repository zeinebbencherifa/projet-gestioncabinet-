package com.example.cabinetgestion.security;

import com.example.cabinetgestion.service.ServiceUtilisateur;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // ✔ Déclare le service utilisateur comme UserDetailsService (sans cercle)
    @Bean
    public UserDetailsService userDetailsService(ServiceUtilisateur serviceUtilisateur) {
        return serviceUtilisateur;
    }


    @Bean
    //ici on définit les urls définis, qui peux acceder a quoi
    public SecurityFilterChain filterChain(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {

        http
                .csrf(csrf -> csrf.disable())//protége des attaques
                .userDetailsService(userDetailsService)//Indique à Spring Security de charger les utilisateurs via ServiceUtilisateur
                .authorizeHttpRequests(auth -> auth //les routes qui sontta ccesible memme sans login
                        .requestMatchers("/", "/login", "/register", "/css/**").permitAll()


                        .requestMatchers("/medecin/**", "/home").hasAuthority("MEDECIN")//seumlment les medecin ont l'acces aux pages qui commence /medecin/


                        .requestMatchers("/patient/save").hasAnyAuthority("MEDECIN", "PATIENT")
                        .requestMatchers("/patient/**").hasAuthority("PATIENT")


                        .anyRequest().authenticated()//les autres retoutes nécessite que l'utilisateur soit connectes
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .failureUrl("/login?error=true")
                        .defaultSuccessUrl("/redirect", true)
                        .permitAll()
                )
                .logout(log -> log.logoutUrl("/logout").permitAll());

        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }//BCrypt pour crypter les mots de passe.
}
