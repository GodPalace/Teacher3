package com.godpalace.student.module.file;

import lombok.Getter;

@Getter
public enum RemoteFileType {
    ROOT(0), FILE(1), DIRECTORY(2);

    private final int type;
    RemoteFileType(int type) {
        this.type = type;
    }

    public static RemoteFileType getRemoteFileType(int type) {
        for (RemoteFileType remoteFileType : values()) {
            if (remoteFileType.getType() == type) {
                return remoteFileType;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return switch (type) {
            case 0 -> "磁盘";
            case 1 -> "文件";
            case 2 -> "文件夹";
            default -> "未知";
        };
    }
}
