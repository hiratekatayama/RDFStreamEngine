package Parallel.test;

import Parallel.GridTest;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//サーバ
public class sampleGridTest implements Callable<String> {
    public static void main(String[] args) {
        //仕事をさせる対象のGridのリスト
        InetSocketAddress[] gridList = new InetSocketAddress[] {
                new InetSocketAddress("127.0.0.1", 5100),
                new InetSocketAddress("127.0.0.1", 5101)
        };

        ExecutorService service = Executors.newCachedThreadPool();
        sampleGridTest gridTest;

        //すべてのGridに10回ずつ仕事を投げる
        for (int i = 0; i < 10; i++) {
            for (InetSocketAddress gridList1 : gridList) {
                gridTest = new sampleGridTest(gridList1);
                System.out.println("仕事を割り振る: " + gridList1);
                service.submit(gridTest);
            }
        }
    }

    private InetSocketAddress address = null;

    public sampleGridTest(InetSocketAddress address) {
        this.address = address;
    }

    //GridへのSocketをオープンして，requestを投げて，responseを受け取る
    @Override
    public String call() throws Exception {
        Socket socket = null;
        BufferedWriter writer = null;
        BufferedReader reader = null;
        String request = "Hello, who are you?";

        try {
            socket = new Socket();
            //クライアントに接続
            socket.connect(this.address, 3000);

            //サーバへ送信するデータを書き込み
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            //サーバからの返信を読み込み
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

            writer.write(request + "\n");
            writer.flush();
            String response = reader.readLine();
            System.out.println(System.currentTimeMillis() + " " + "response from" + this.address.getHostName() + ":" + this.address.getPort() + " [" + response + "]");




        } catch (IOException e) {
        }

        return request;
    }
}
