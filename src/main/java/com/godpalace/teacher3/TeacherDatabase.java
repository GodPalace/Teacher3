package com.godpalace.teacher3;

import com.godpalace.data.annotation.Data;
import com.godpalace.data.annotation.LocalDatabase;
import com.godpalace.data.database.FileDatabaseEngine;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
@LocalDatabase(path = ".\\Teacher.db")
public class TeacherDatabase {
    @Data
    public static String password = "7f2611ba158b6dcea4a69c229c303358c5e04493abeadee106a4bfa464d55787"; // password for hashing

    public static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors() * 4;

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
