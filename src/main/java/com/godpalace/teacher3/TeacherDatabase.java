package com.godpalace.teacher3;

import com.godpalace.data.annotation.LocalDatabase;
import com.godpalace.data.database.FileDatabaseEngine;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
@LocalDatabase(path = "C:\\Users\\Public\\.godpalace\\teacher3\\teacher.db")
public class TeacherDatabase {
    public static void initialize() {
        try {
            new File("C:\\Users\\Public\\.godpalace\\teacher3").mkdirs();

            FileDatabaseEngine.init(TeacherDatabase.class, null);
            log.info("Database initialized");
        } catch (Exception e) {
            log.error("Error initializing database", e);
        }
    }
}
