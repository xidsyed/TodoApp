package com.example.todoapp.config

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.migration.*
import org.springframework.beans.factory.config.*
import org.springframework.context.annotation.*
import org.springframework.core.env.Environment
import org.springframework.core.io.FileSystemResource
import org.springframework.jdbc.datasource.init.ScriptUtils
import java.io.File

@Configuration
class FlywayTestConfig {

    @Bean(initMethod = "migrate")
    fun flyway(env: Environment): Flyway {
        return Flyway.configure()
            .dataSource(
                env.getRequiredProperty("spring.flyway.url"),
                env.getRequiredProperty("spring.flyway.user"),
                env.getRequiredProperty("spring.flyway.password")
            )
            .locations(*env.getRequiredProperty("spring.flyway.locations").split(",").toTypedArray())
            // Match the file naming convention: 20251204230200_quiz.sql
            .sqlMigrationPrefix("") 
            .sqlMigrationSeparator("_")
            .baselineOnMigrate(true)
            // Register the seed migration
            .javaMigrations(R__Seed())
            .load()
    }

    @Bean
    fun flywayDependencyPostProcessor(): BeanFactoryPostProcessor {
        return BeanFactoryPostProcessor { beanFactory: ConfigurableListableBeanFactory ->
            if (beanFactory.containsBeanDefinition("connectionFactory")) {
                val beanDefinition = beanFactory.getBeanDefinition("connectionFactory")
                beanDefinition.setDependsOn("flyway")
            }
        }
    }

    // Java Migration that wraps the seed.sql file
    class R__Seed : BaseJavaMigration() {
        override fun migrate(context: Context) {
            val seedFile = File("newz_supabase/supabase/seed.sql")
            if (seedFile.exists()) {
                // Use ScriptUtils to execute the SQL script robustly
                ScriptUtils.executeSqlScript(context.connection, FileSystemResource(seedFile))
            }
        }
        
        // We need to override getChecksum so Flyway knows when the file changes
        override fun getChecksum(): Int? {
            val seedFile = File("newz_supabase/supabase/seed.sql")
            return if (seedFile.exists()) {
                val crc32 = java.util.zip.CRC32()
                crc32.update(seedFile.readBytes())
                crc32.value.toInt()
            } else {
                null
            }
        }
    }
}
