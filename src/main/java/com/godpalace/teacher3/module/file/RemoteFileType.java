package com.godpalace.teacher3.module.file;

import javafx.scene.image.Image;
import lombok.Getter;

import java.util.Objects;

public enum RemoteFileType {
    ROOT(0), FILE(1), DIRECTORY(2);

    private static final Image ROOT_ICON = new Image(Objects.requireNonNull(RemoteFileType.class.getResourceAsStream("/icon/file/Disk.png")));
    private static final Image FILE_ICON = new Image(Objects.requireNonNull(RemoteFileType.class.getResourceAsStream("/icon/file/File.png")));
    private static final Image DIRECTORY_ICON = new Image(Objects.requireNonNull(RemoteFileType.class.getResourceAsStream("/icon/file/Mkdir.png")));

    @Getter
    private final int type;

    RemoteFileType(int type) {
        this.type = type;
    }

    public Image getIcon() {
        return switch (type) {
            case 0 -> ROOT_ICON;
            case 1 -> FILE_ICON;
            case 2 -> DIRECTORY_ICON;
            default -> null;
        };
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
