package Parallel.Socket_Channel;
/**
 * @author: Toshiyuki Hiakata
 * クライアント側
 */

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class WebClient {
    public static void main(String[] args) throws IOException {
        //1. SocketChannelのOpen()メソッドでSocketChannelインスタンスを作る
        SocketChannel socketChannel = SocketChannel.open();

        //2. サーバへ接続
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 3333));

        //3. サーバへの送信データを準備する
        ByteBuffer writeBuffer = ByteBuffer.allocate(128);
        writeBuffer.put("hello WebServer this is from Web Client".getBytes());
        writeBuffer.flip();
        socketChannel.write(writeBuffer);
        ByteBuffer readBuffer = ByteBuffer.allocate(128);
        socketChannel.read(readBuffer);
        StringBuffer stringBuffer = new StringBuffer();

        //4. サーバからのデータを受け取り
        readBuffer.flip();
        while (readBuffer.hasRemaining()) {
            stringBuffer.append((char) readBuffer.get());
        }
        System.out.println("サーバからのメッセージ："+ stringBuffer);
        socketChannel.close();
    }
}
