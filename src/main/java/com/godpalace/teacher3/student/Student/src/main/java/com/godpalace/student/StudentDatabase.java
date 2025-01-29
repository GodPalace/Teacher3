package com.godpalace.student;

import com.godpalace.data.annotation.Data;
import com.godpalace.data.annotation.LocalDatabase;
import com.godpalace.data.database.FileDatabaseEngine;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
@LocalDatabase(path = "C:\\Users\\Public\\.godpalace\\student\\student.db")
public class StudentDatabase {
    @Data
    public static String lastDllOutPath = "";

    public static void initialize() {
        try {
            new File("C:\\Users\\Public\\.godpalace\\student").mkdirs();

            FileDatabaseEngine.init(StudentDatabase.class, null);
            log.info("Database initialized");
        } catch (Exception e) {
            log.error("Error initializing database", e);
        }
    }
}
