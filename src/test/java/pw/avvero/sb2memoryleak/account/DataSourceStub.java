package pw.avvero.sb2memoryleak.account;

import org.flywaydb.core.Flyway;
import org.postgresql.jdbc2.optional.SimpleDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@TestConfiguration
public class DataSourceStub {
    private static final String CLEAN_FORMAT = "DROP SCHEMA IF EXISTS %s CASCADE; CREATE SCHEMA %s;";
    private static final String CLEAN_PUBLIC = "DROP SCHEMA IF EXISTS public CASCADE; CREATE SCHEMA public;";
    private static final String SELECT_FORMAT = "ALTER USER %s SET search_path to %s;";

    private final static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:10")
            .withCommand("postgres -c max_connections=300");

    static {
        POSTGRES.start();
    }

    @Value("${spring.jpa.properties.hibernate.default_schema}")
    private String schema;

    @Value("${spring.flyway.table}")
    private String table;

    @Value("${spring.flyway.schemas}")
    private String schemas;

    @Bean
    public DataSource dataSource() throws SQLException {
        SimpleDataSource ds = new SimpleDataSource();
        ds.setUser(POSTGRES.getUsername());
        ds.setPassword(POSTGRES.getPassword());
        ds.setUrl(POSTGRES.getJdbcUrl());

        try (Connection connection = ds.getConnection()) {
            connection.prepareStatement(CLEAN_PUBLIC).execute();
            connection.prepareStatement(String.format(CLEAN_FORMAT, schema, schema)).execute();
            connection.prepareStatement(String.format(SELECT_FORMAT, POSTGRES.getUsername(), schema)).execute();
        }

        Flyway.configure()
                .dataSource(ds)
                .schemas(schemas)
                .table(table)
                .load()
                .migrate();

        return ds;
    }
}
