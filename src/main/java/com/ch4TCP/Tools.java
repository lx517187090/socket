package com.ch4TCP;

public class Tools {

    public static int byteArrayToInt(byte[] buff){
        return buff[3] & 0xFF | (buff[2] & 0xFF) << 8 | (buff[1] & 0xFF) << 16 | (buff[0] & 0xFF) << 24;
    }

    public static byte[] intToByteArray(int a){
        return new byte[]{
                (byte)((a >> 24) & 0xFF), (byte)((a >> 16) & 0xFF), (byte)((a >> 8) & 0xFF), (byte)(a & 0xFF)
        };
    }
}
