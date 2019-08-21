package com.socket.core;

import java.io.Closeable;
import java.io.IOException;

public class IoContext implements Closeable {

    private static IoContext INSTANCE;

    private final IoProvider ioProvider;

    public IoContext(IoProvider ioProvider) {
        this.ioProvider = ioProvider;
    }

    public IoProvider getIoProvider(){
        return ioProvider;
    }

    public static IoContext get(){
        return INSTANCE;
    }

    public static StartBoot setup(){
        return new StartBoot();
    }

    @Override
    public void close() throws IOException {
        ioProvider.close();
    }

    public static class StartBoot{
        private IoProvider ioProvider;

        private StartBoot(){}

        public StartBoot ioProvider(IoProvider ioProvider){
            this.ioProvider = ioProvider;
            return this;
        }
        public IoContext start(){
            INSTANCE = new IoContext(ioProvider);
            return INSTANCE;
        }
    }
}
