package com.godpalace.student.module.file;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public record RemoteFile(String path, RemoteFileType type) implements Serializable {
    public void writeToStream(ObjectOutputStream out) throws IOException {
        out.writeUTF(path);
        out.writeInt(type.getType());
    }
}
