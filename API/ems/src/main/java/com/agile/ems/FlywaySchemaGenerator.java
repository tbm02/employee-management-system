package com.agile.ems;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FlywaySchemaGenerator {

    private static final String MIGRATION_PATH =
            "src/main/resources/db/migration/";

    public static void main(String[] args) {



        String migrationName = "department_entity_creation";

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        String fileName = "V" + timestamp + "__" + migrationName + ".sql";

        File directory = new File(MIGRATION_PATH);

        if (!directory.exists()) {
            directory.mkdirs();
        }

        File migrationFile = new File(directory, fileName);

        try (FileWriter writer = new FileWriter(migrationFile)) {

            writer.write("-- Migration: " + migrationName + "\n");
            writer.write("-- Created at: " + LocalDateTime.now() + "\n\n");
            writer.write("BEGIN;\n\n");
            writer.write("-- Write your SQL here\n\n\n");
            writer.write("COMMIT;\n");

            System.out.println("✅ Migration created: " + fileName);

        } catch (IOException e) {
            System.out.println("❌ Error creating migration file");
            e.printStackTrace();
        }
    }
}