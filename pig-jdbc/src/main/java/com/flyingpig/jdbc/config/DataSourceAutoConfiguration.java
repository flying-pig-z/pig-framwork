package com.flyingpig.jdbc.config;

import com.flyingpig.jdbc.DataSource;
import com.flyingpig.jdbc.JdbcTemplate;
import com.flyingpig.jdbc.SimpleDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceAutoConfiguration {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;


    @Bean
    public DataSource dataSource(DataSourceProperties properties) {
        SimpleDataSource dataSource = new SimpleDataSource();
        dataSource.setUrl(properties.getUrl());
        dataSource.setUsername(properties.getUsername());
        dataSource.setPassword(properties.getPassword());
        return dataSource;
    }

    @Bean
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties(url, username, password);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}