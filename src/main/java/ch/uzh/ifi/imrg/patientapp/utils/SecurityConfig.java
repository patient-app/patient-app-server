package ch.uzh.ifi.imrg.patientapp.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import ch.uzh.ifi.imrg.patientapp.security.CoachKeyFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final CoachKeyFilter coachKeyFilter;

  public SecurityConfig(CoachKeyFilter coachKeyFilter) {
    this.coachKeyFilter = coachKeyFilter;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable()).addFilterBefore(coachKeyFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(auth -> auth.requestMatchers("/coach/register").permitAll()
            .requestMatchers("/coach/patients/**").hasRole("COACH").anyRequest().permitAll());

    return http.build();
  }
}
