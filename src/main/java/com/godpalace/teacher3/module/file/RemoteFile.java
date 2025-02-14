package com.godpalace.teacher3.module.file;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public record RemoteFile(String path, RemoteFileType type) implements Serializable {
    public static RemoteFile readRemoteFileFromStream(ObjectInputStream in) throws IOException {
        String path = in.readUTF();
        RemoteFileType type = RemoteFileType.getRemoteFileType(in.readInt());

        return new RemoteFile(path, type);
    }

    public String name() {
        return path.substring(path.lastIndexOf(File.separator) + 1);
    }
}
