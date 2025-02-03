package com.godpalace.student.module;

import com.backblaze.erasure.FecAdapt;
import com.godpalace.data.database.ImageSerialization;
import com.godpalace.student.Teacher;
import com.godpalace.student.manager.ThreadPoolManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import kcp.ChannelConfig;
import kcp.KcpClient;
import kcp.KcpListener;
import kcp.Ukcp;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DisplayModule implements Module {
    private static final short CAPTURING = 0x01;
    private static final short START     = CAPTURING + 1;
    private static final short STOP      = START + 1;

    private static final ConcurrentHashMap<Integer, KcpClient> clients = new ConcurrentHashMap<>();
    private static final Random random = new Random();
    private static final Robot robot;

    private final ChannelConfig channelConfig = new ChannelConfig();

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    public DisplayModule() {
        channelConfig.nodelay(true,40,2,true);
        channelConfig.setSndwnd(512);
        channelConfig.setRcvwnd(512);
        channelConfig.setMtu(512);
        channelConfig.setAckNoDelay(true);
        channelConfig.setConv(getID());
        channelConfig.setFecAdapt(new FecAdapt(3,1));
        channelConfig.setCrc32Check(true);
    }

    @Override
    public short getID() {
        return 0x08;
    }

    @Override
    public String getName() {
        return "DisplayModule";
    }

    @Override
    public ByteBuf execute(Teacher teacher, ByteBuf data) throws Exception {
        switch (data.readShort()) {
            case START -> {
                String ip = teacher.getIp();
                int port = data.readInt();

                KcpClient client = new KcpClient();
                client.init(channelConfig);
                client.connect(new InetSocketAddress(ip, port), channelConfig, new DisplayHandler());

                int id = random.nextInt(1000000);
                while (clients.containsKey(id)) id = random.nextInt(1000000);
                clients.put(id, client);

                ByteBuf buf = Unpooled.buffer(4);
                buf.writeInt(id);

                return buf;
            }

            case STOP -> {
                int id = data.readInt();
                KcpClient client = clients.remove(id);

                if (client != null) {
                    client.stop();
                }
            }

            case CAPTURING -> {
                Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
                BufferedImage image = robot.createScreenCapture(new Rectangle(dimension));

                return Unpooled.wrappedBuffer(new ImageSerialization(image).image);
            }
        }

        return null;
    }

    @Override
    public boolean isLocalModule() {
        return false;
    }

    static class DisplayHandler implements KcpListener {
        @Override
        public void onConnected(Ukcp ukcp) {
            ThreadPoolManager.getExecutor().execute(() -> {
                while (true) {
                    try {
                        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
                        BufferedImage image = robot.createScreenCapture(new Rectangle(dimension));
                        ImageSerialization serialization = new ImageSerialization(image);

                        ByteBuf buf = Unpooled.buffer(serialization.image.length);
                        buf.writeBytes(serialization.image);

                        ukcp.write(buf);
                        buf.release();

                        synchronized (this) {
                            wait(10);
                        }
                    } catch (Exception e) {
                        log.error("DisplayHandler onConnected", e);
                        ukcp.close();
                        break;
                    }
                }
            });
        }

        @Override
        public void handleReceive(ByteBuf byteBuf, Ukcp ukcp) {
        }

        @Override
        public void handleException(Throwable throwable, Ukcp ukcp) {
            log.error("DisplayHandler handleException", throwable);
            ukcp.close();
        }

        @Override
        public void handleClose(Ukcp ukcp) {
        }
    }
}
