package com.webcrafters.gatekeeperback.core.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtAuthenticationFilter: JwtAuthenticationFilter,
    ): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                // 1. Rotas de Autenticação (Abertas para todos)
                it.requestMatchers("/api/auth/**").permitAll()

                // 2. Rotas do Admin (Somente usuários com Role ADMIN)
                it.requestMatchers("/api/admin/**").hasRole("ADMIN")

                // 3. Rotas do Gestor (Somente usuários com Role MANAGER)
                it.requestMatchers("/api/manager/**").hasRole("MANAGER")

                // 4. Rotas do Portador (Somente usuários com Role CARDHOLDER)
                it.requestMatchers("/api/cardholder/**").hasRole("CARDHOLDER")

                // 5. Qualquer outra rota (ex: Swagger futuro) exige apenas estar logado
                it.anyRequest().authenticated()
            }
            .cors(Customizer.withDefaults())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}