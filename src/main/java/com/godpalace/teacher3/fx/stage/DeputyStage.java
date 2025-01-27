package com.godpalace.teacher3.fx.stage;

import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class DeputyStage extends Stage {
    private Type type = Type.RIGHT;
    private int insets = 5;
    private Size size = Size.MEDIUM;

    public DeputyStage() {
        this("");
    }

    public DeputyStage(String title, Type type, int insets, Size size) {
        this(title);

        this.type = type;
        this.insets = insets;
        this.size = size;
    }

    public DeputyStage(String title) {
        super();

        this.setTitle(title);
        this.setResizable(false);
        this.initStyle(StageStyle.TRANSPARENT);
    }

    @Override
    public void showAndWait() {
        throw new UnsupportedOperationException("This method is not supported in this class.");
    }

    public void show(Stage owner) {
        this.setAlwaysOnTop(owner.isAlwaysOnTop());

        this.setY(owner.getY());
        owner.yProperty().addListener((obs, oldVal, newVal) -> this.setY(newVal.doubleValue()));

        switch (size) {
            case SMALL -> {
                this.setWidth(owner.getHeight() / 4);
                this.setHeight(owner.getHeight() / 2);

                owner.widthProperty().addListener((obs, oldVal, newVal) -> {
                    switch (type) {
                        case LEFT -> this.setX(owner.getX() - owner.getWidth() - insets);
                        case RIGHT -> this.setX(owner.getX() + owner.getWidth() + insets);
                    }

                    this.setWidth(owner.getHeight() / 4);
                    this.setHeight(owner.getHeight() / 2);
                });

                owner.heightProperty().addListener((obs, oldVal, newVal) -> {
                    switch (type) {
                        case LEFT -> this.setX(owner.getX() - this.getWidth() - insets);
                        case RIGHT -> this.setX(owner.getX() + owner.getWidth() + insets);
                    }

                    this.setWidth(newVal.doubleValue() / 4);
                    this.setHeight(newVal.doubleValue() / 2);
                });
            }

            case MEDIUM -> {
                this.setWidth(owner.getHeight() / 2);
                this.setHeight(owner.getHeight() / 2);

                owner.widthProperty().addListener((obs, oldVal, newVal) -> {
                    switch (type) {
                        case LEFT -> this.setX(owner.getX() - this.getWidth() - insets);
                        case RIGHT -> this.setX(owner.getX() + owner.getWidth() + insets);
                    }

                    this.setWidth(owner.getHeight() / 2);
                    this.setHeight(owner.getHeight() / 2);
                });

                owner.heightProperty().addListener((obs, oldVal, newVal) -> {
                    switch (type) {
                        case LEFT -> this.setX(owner.getX() - this.getWidth() - insets);
                        case RIGHT -> this.setX(owner.getX() + owner.getWidth() + insets);
                    }

                    this.setWidth(newVal.doubleValue() / 2);
                    this.setHeight(newVal.doubleValue() / 2);
                });
            }

            case LARGE -> {
                this.setWidth(owner.getHeight());
                this.setHeight(owner.getHeight());

                owner.widthProperty().addListener((obs, oldVal, newVal) -> {
                    this.setWidth(owner.getHeight());
                    this.setHeight(owner.getHeight());
                });

                owner.heightProperty().addListener((obs, oldVal, newVal) -> {
                    this.setWidth(newVal.doubleValue());
                    this.setHeight(newVal.doubleValue());
                });
            }
        }

        switch (type) {
            case LEFT -> {
                this.setX(owner.getX() - owner.getWidth() - insets);

                owner.xProperty().addListener((obs, oldVal, newVal) ->
                        this.setX(newVal.doubleValue() - owner.getWidth() - insets));
            }

            case RIGHT -> {
                this.setX(owner.getX() + owner.getWidth() + insets);

                owner.xProperty().addListener((obs, oldVal, newVal) ->
                        this.setX(newVal.doubleValue() + owner.getWidth() + insets));
            }
        }

        this.show();
    }

    public enum Type {
        LEFT, RIGHT
    }

    public enum Size {
        SMALL, MEDIUM, LARGE
    }
}
