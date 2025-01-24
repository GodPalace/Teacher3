package com.godpalace.student.module;

import com.godpalace.student.Teacher;

import java.io.File;
import java.io.FileWriter;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RunRecordModule implements Module {
    @Override
    public short getID() {
        return -0x06;
    }

    @Override
    public String getName() {
        return "RunRecordModule";
    }

    @Override
    public void execute(Teacher teacher, ByteBuffer data) throws Exception {
        File file = new File("C:\\Users\\Public\\.godpalace\\student\\run_record.txt");
        if (file.length() > 102400) {
            file.delete();
        }

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        FileWriter writer = new FileWriter(file, true);
        writer.write(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ")
                .format(new Date()) + "Started student\n");

        writer.close();
    }

    @Override
    public boolean isLocalModule() {
        return true;
    }
}
