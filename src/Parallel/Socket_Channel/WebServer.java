package Parallel.Socket_Channel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class WebServer {
    public static void main(String[] args) {
        try {
            //1. ServerSocketChannelのopen()メソッドでServerSocketChannelのインスタンスを作る
            ServerSocketChannel ssc = ServerSocketChannel.open();

            //2. ipとportをバインド
            ssc.socket().bind(new InetSocketAddress("127.0.0.1", 3333));
            //ServerSocketChannelImplのaccept()メソッドでSocketChannelインスタンスをもらってクライアントへの書き込み
            SocketChannel socketChannel = ssc.accept();

            //3. 書き出すデータを準備する
            ByteBuffer writeBuffer = ByteBuffer.allocate(128);
            writeBuffer.put("hello WebClient this is from WebServer".getBytes());
            writeBuffer.flip();
            socketChannel.write(writeBuffer);
            ByteBuffer readBuffer = ByteBuffer.allocate(128);

            //4. 読み込むデータを準備する
            socketChannel.read(readBuffer);
            StringBuilder stringBuffer = new StringBuilder();

            readBuffer.flip();
            while (readBuffer.hasRemaining()) {
                stringBuffer.append((char) readBuffer.get());
            }
            System.out.println("クライアントからのデータを受け取り:" + stringBuffer);
            socketChannel.close();
            ssc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
