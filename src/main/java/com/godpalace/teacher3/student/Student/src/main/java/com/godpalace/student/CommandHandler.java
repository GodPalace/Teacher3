package com.godpalace.student;

import com.godpalace.student.manager.ModuleManager;
import com.godpalace.student.module.Module;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

/*
 * 数据包格式：
 * 2字节: 命令ID(已处理)
 * 2字节: 时间戳(已处理)
 * 数据:  字节数组(未处理)
 */

@Slf4j
public class CommandHandler {
    public static void handleCommand(Teacher teacher, ByteBuf buffer) {
        HashMap<Short, Module> modules = ModuleManager.getModules();

        if (buffer.readableBytes() < 4) {
            log.warn("Invalid command packet");
            return;
        }

        short moduleId = buffer.readShort();
        short timestamp = buffer.readShort();

        try {
            if (modules.containsKey(moduleId)) {
                log.debug("Executing module: {}", modules.get(moduleId).getName());

                ByteBuf response = modules.get(moduleId).execute(teacher, buffer);
                if (response != null) {
                    teacher.sendResponse(moduleId, timestamp, response);
                    ReferenceCountUtil.release(response);
                }
            } else {
                log.warn("Unknown command: {}", moduleId);
            }
        } catch (Exception e) {
            log.error("Error executing command", e);
        }

        buffer.release();
    }
}
