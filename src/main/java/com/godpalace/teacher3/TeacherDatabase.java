package com.godpalace.teacher3;

import com.godpalace.data.annotation.Data;
import com.godpalace.data.annotation.LocalDatabase;
import com.godpalace.data.database.FileDatabaseEngine;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
@LocalDatabase(path = "C:\\Users\\Public\\.godpalace\\teacher3\\teacher.db")
public class TeacherDatabase {
    @Data
    public static String password = "password";

    public static void initialize() {
        try {
            new File("C:\\Users\\Public\\.godpalace\\teacher3").mkdirs();

            FileDatabaseEngine.init(TeacherDatabase.class, null);
            log.debug("Database initialized");
        } catch (Exception e) {
            log.error("Error initializing database", e);
        }
    }
}
