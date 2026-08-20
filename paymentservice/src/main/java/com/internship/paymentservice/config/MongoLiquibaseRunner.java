package com.internship.paymentservice.config;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoLiquibaseRunner {

    @Bean
    public Object runMongoLiquibase(
            @Value("${spring.data.mongodb.uri}") String mongoUri
    ) {
        Database database = null;

        try {
            database = DatabaseFactory.getInstance().openDatabase(
                    mongoUri,
                    null,
                    null,
                    null,
                    new ClassLoaderResourceAccessor()
            );

            Liquibase liquibase = new Liquibase(
                    "db/changelog/db.changelog-master.xml",
                    new ClassLoaderResourceAccessor(),
                    database
            );

            liquibase.update(new Contexts(), new LabelExpression());

            return new Object();
        } catch (Exception exception) {
            if (database != null) {
                try {
                    database.close();
                } catch (Exception closeException) {
                    exception.addSuppressed(closeException);
                }
            }

            throw new IllegalStateException("Failed to run MongoDB Liquibase migrations", exception);
        }
    }
}