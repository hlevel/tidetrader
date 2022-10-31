package tide.trader.bot.common.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
@Order(9)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${trading.bot.security.username}")
    private String username;

    @Value("${trading.bot.security.password}")
    private String password;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                //.antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/", "/index", "/strategy/**", "/balances", "/graphiql" , "/graphql/**").hasAnyRole("USER")
                //.antMatchers("/", "/**").permitAll()
                .antMatchers("/webjars/**", "/css/**", "/js/**").permitAll()
                .and().formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/index", true)
                .and().logout()
                .and().rememberMe()
                .and()
                .csrf().disable();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //auth.inMemoryAuthentication().withUser(username).password(password).roles("USER");
        auth.inMemoryAuthentication().passwordEncoder(new BCryptPasswordEncoder()).withUser(username)
                .password(new BCryptPasswordEncoder().encode(password)).roles("USER");
    }


}
