package com.godpalace.teacher3.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class EncryptHandler extends MessageToMessageEncoder<ByteBuf> {
    private static byte[] key = "Teacher_v3%Password".getBytes();
    private static Cipher cipher;

    static {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // 截取16字节作为AES密钥
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        } catch (Exception e) {
            log.error("初始化加密处理器失败", e);
        }
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        byte[] array = new byte[msg.readableBytes()];
        msg.getBytes(msg.readerIndex(), array);
        byte[] encrypted = cipher.doFinal(array); // 加密数据
        out.add(Unpooled.copiedBuffer(encrypted));
    }
}