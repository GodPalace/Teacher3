module Teacher {
    requires com.github.kwhat.jnativehook;
    requires GLDatabase;
    requires io.netty.all;
    requires java.datatransfer;
    requires java.desktop;
    requires java.management;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires kcp.base;
    requires kcp.fec;
    requires static lombok;
    requires org.kordamp.ikonli.boxicons;
    requires org.kordamp.ikonli.javafx;
    requires org.slf4j;
    requires org.apache.logging.log4j.core;
    requires org.apache.logging.log4j.slf4j;

    opens com.godpalace.teacher3;
    exports com.godpalace.teacher3;
    uses com.godpalace.teacher3.Main;
}
