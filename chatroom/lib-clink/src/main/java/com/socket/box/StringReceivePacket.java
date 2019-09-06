package com.socket.box;

import com.socket.core.ReceivePacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * 接收String
 * Byte数组--->String
 */
public class StringReceivePacket extends ReceivePacket<ByteArrayOutputStream> {

    private String string;

    public StringReceivePacket(int len) {
        length = len;
    }

    public String string(){
        return string;
    }

    @Override
    protected void closeStream(ByteArrayOutputStream stream) throws IOException {
        super.closeStream(stream);
        string = new String(stream.toByteArray());
    }

    @Override
    protected ByteArrayOutputStream createStream() {
        return new ByteArrayOutputStream((int)length);
    }

    @Override
    public void save(byte[] bytes, int count) {

    }
}
