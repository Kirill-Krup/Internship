package com.internship.paymentservice.config;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoLiquibaseRunner {

    @Bean
    ApplicationRunner runMongoLiquibase(
            @Value("${spring.data.mongodb.uri}") String mongoUri
    ) {
        return args -> {
            Database database = DatabaseFactory.getInstance().openDatabase(
                    mongoUri,
                    null,
                    null,
                    null,
                    new ClassLoaderResourceAccessor()
            );

            try (database;
                 Liquibase liquibase = new Liquibase(
                         "db/changelog/db.changelog-master.xml",
                         new ClassLoaderResourceAccessor(),
                         database
                 )) {

                liquibase.update(new Contexts(), new LabelExpression());
            }
        };
    }
}