module com.godpalace.teacher {
    requires com.github.kwhat.jnativehook;
    requires GLDatabase;
    requires io.netty.all;
    requires java.datatransfer;
    requires java.desktop;
    requires java.management;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires kcp.base;
    requires kcp.fec;
    requires static lombok;
    requires org.kordamp.ikonli.boxicons;
    requires org.kordamp.ikonli.javafx;
    requires org.pomo.toasterfx;

    exports com.godpalace.teacher3;
    exports com.godpalace.teacher3.fx.menu.help.about to javafx.fxml;
    exports com.godpalace.teacher3.fx.menu.help.help to javafx.fxml;

    opens com.godpalace.teacher3;
    opens com.godpalace.teacher3.fx.menu.help.about to javafx.fxml;
    opens com.godpalace.teacher3.fx.menu.help.help to javafx.fxml;
}