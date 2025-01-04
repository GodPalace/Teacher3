package com.godpalace.student;

import com.godpalace.data.annotation.LocalDatabase;
import com.godpalace.data.database.FileDatabaseEngine;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@LocalDatabase(path = "C:\\Users\\Public\\.godpalace\\student\\student.db")
public class StudentDatabase {
    public static void initialize() {
        try {
            FileDatabaseEngine.init(StudentDatabase.class, null);
            log.info("Database initialized");
        } catch (Exception e) {
            log.error("Error initializing database", e);
        }
    }
}
