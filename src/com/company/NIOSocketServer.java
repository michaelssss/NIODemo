package com.company;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NIOSocketServer
{
    private ByteBuffer buffer = ByteBuffer.allocate(20);

    private char[] output = new char[200];

    private int flag = 0;

    public NIOSocketServer(int port)
        throws IOException
    {
        Selector selector = Selector.open();
        //Open Channel
        ServerSocketChannel ssc = ServerSocketChannel.open();
        //setup nonBlocking mode
        ssc.configureBlocking(false);
        ServerSocket sc = ssc.socket();
        InetSocketAddress address = new InetSocketAddress(port);
        //start listening
        sc.bind(address);
        //when request accept
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("listening port " + port);

        for (; ; )
        {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator it = selectedKeys.iterator();
            while (it.hasNext())
            {
                SelectionKey k = (SelectionKey)it.next();
                it.remove();
                if (k.isAcceptable())
                {
                    ServerSocketChannel ssc1 = (ServerSocketChannel)k.channel();
                    SocketChannel sc1 = ssc1.accept();
                    sc1.configureBlocking(false);
                    SelectionKey newKey = sc1.register(selector, SelectionKey.OP_READ);
                    System.out.println("accepted a new incomming connection");
                    String Hello = "Hello Guys\r\n";
                    sc1.write(ByteBuffer.wrap(Hello.getBytes()));
                }
                if (k.isReadable())
                {

                    // Read the data
                    SocketChannel sc2 = (SocketChannel)k.channel();
                    String ip = sc2.getRemoteAddress().toString();
                    while (true)
                    {
                        buffer.clear();
                        int num = sc2.read(buffer);
                        if (num <= 0)
                            break;

                        byte tm = buffer.get(0);
                        if (tm != 13)
                        {
                            if (tm == 27)
                            {
                                sc2.write(ByteBuffer.wrap("\r\nGood Bye\r\n".getBytes()));
                                System.out.println("IP" + sc2.getRemoteAddress() + " Disconnect");
                                sc2.close();
                                break;
                            }
                            flag++;
                            output[flag] = (char)tm;

                        }
                        else if (tm == 13)
                        {
                            sc2.write(ByteBuffer.wrap(("\r\n" + "You Have Write This:" +
                                new String(output).trim() + "\r\n").getBytes()));
                            System.out.println("New Message:" + new String(output).trim());
                            output = new char[200];
                            flag = 0;
                        }
                    }
                    if (sc2.isOpen() && sc2.read(buffer) == -1)
                    {
                        System.out.println("IP" + ip + " lost connection");
                        sc2.close();
                    }
                }
            }

        }
    }

    public static void main(String[] args)
    {
        try
        {
            new NIOSocketServer(8080);
        }
        catch (IOException ie)
        {
            ie.printStackTrace();
        }
    }

}
