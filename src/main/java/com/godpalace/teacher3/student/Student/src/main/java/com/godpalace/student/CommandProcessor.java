package com.godpalace.student;

import com.godpalace.student.module.Module;
import com.godpalace.student.module.ModuleManager;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.HashMap;

/*
 * 数据包格式：
 * 2字节：命令ID(已处理)
 * 4字节：数据长度(已处理)
 * 数据：字节数组(未处理)
 */

@Slf4j
public class CommandProcessor {
    public static void handle(Teacher teacher) throws Exception {
        HashMap<Short, Module> modules = ModuleManager.getModules();

        ByteBuffer buffer = ByteBuffer.allocate(2);
        teacher.getChannel().read(buffer);
        buffer.flip();
        short cmd = buffer.getShort();

        buffer = ByteBuffer.allocate(4);
        teacher.getChannel().read(buffer);
        buffer.flip();
        int length = buffer.getInt();

        buffer = ByteBuffer.allocate(length);
        teacher.getChannel().read(buffer);
        buffer.flip();

        if (modules.containsKey(cmd)) {
            log.debug("Executing module: {}", modules.get(cmd).getName());
            modules.get(cmd).execute(teacher, buffer);
        } else {
            log.warn("Unknown command: {}", cmd);
        }
    }
}
