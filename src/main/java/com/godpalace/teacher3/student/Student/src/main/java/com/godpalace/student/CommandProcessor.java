package com.godpalace.student;

import com.godpalace.student.module.Module;
import com.godpalace.student.util.PackageUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

/*
 * 数据包格式：
 * 2字节：命令ID(已处理)
 * 4字节：数据长度(已处理)
 * 数据：字节数组(未处理)
 */

@Slf4j
public class CommandProcessor {
    private static final HashMap<Short, Module> modules = new HashMap<>();

    public static void initialize() throws Exception {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            List<String> classes = PackageUtil.getClassName("com.godpalace.student.module");
            for (String classPath : classes) {
                String className = classPath.substring(classPath.lastIndexOf(".") + 1);
                if (className.equals("Module")) continue;

                Class<?> clazz = loader.loadClass(classPath);
                if (!Module.class.isAssignableFrom(clazz)) continue;

                Module module = (Module) clazz.getDeclaredConstructor().newInstance();
                short id = module.getID();
                if (modules.containsKey(id)) {
                    log.error("Duplicate module ID: {}", id);
                    continue;
                }

                modules.put(id, module);
                log.debug("Loading module: {}", className);
            }
        } catch (Exception e) {
            log.error("Error initializing modules", e);
            throw new Exception("Error initializing modules", e);
        }
    }

    public static void handle(Teacher teacher) throws Exception {
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
