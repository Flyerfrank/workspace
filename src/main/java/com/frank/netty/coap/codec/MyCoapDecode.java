package com.frank.netty.coap.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.eclipse.californium.elements.util.DatagramReader;

import java.util.List;

public class MyCoapDecode extends MessageToMessageDecoder<DatagramPacket> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket, List<Object> list) throws Exception {
        byte[] array = datagramPacket.content().array();
        DatagramReader reader = new DatagramReader(array);
//        DatagramWriter
        int read = reader.read(2);
        System.out.println("read = " + read);
        int read1 = reader.read(2);
        System.out.println("read1 = " + read1);
        int read2 = reader.read(4);
        System.out.println("read2 = " + read2);
        int read3 = reader.read(8);
        System.out.println("read3 = " + read3);
    }
}
