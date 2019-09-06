package com.socket.box;

import com.socket.core.SendPacket;

import java.io.*;

public class FileSendPacket extends SendPacket<FileInputStream> {


    public FileSendPacket(File file) {
        this.length = file.length();
    }

    @Override
    protected FileInputStream createStream() {
        return null;
    }
}
