package za.co.varl.trading.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Enable method security
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { csrf -> csrf.disable() } // Disable CSRF protection (consider enabling it for production)
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**", "/webjars/**").permitAll() // Allow Swagger UI
                    .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/users", "/api/auth/changepassword").permitAll() // Allow access to register and login
                    .anyRequest().authenticated() // Require authentication for all other requests
            }


        return http.build()
    }
}