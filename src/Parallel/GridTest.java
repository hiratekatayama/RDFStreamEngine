package Parallel;

import org.insight_centre.citybench.main.parallel.CityBench_Parallel;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GridTest implements Callable<String> {

    private static String query;

    public static void main(String[] args) throws IOException {

        //仕事をさせる対象のGrid(サーバ＋ポート)のリスト
        InetSocketAddress[] gridList = new InetSocketAddress[]{
                new InetSocketAddress("127.0.0.1", 5100),
                new InetSocketAddress("127.0.0.1", 5101),
                new InetSocketAddress("127.0.0.1", 5102)
        };

        Worker(gridList);
    }

    private InetSocketAddress address = null;
    public GridTest(InetSocketAddress address) {
        this.address = address;
    }

    public static void Worker(InetSocketAddress[] gridList) {
        ExecutorService service = Executors.newCachedThreadPool();
        GridTest gridTest;
        //すべてのGridに仕事を投げる
        for (InetSocketAddress gridList1 : gridList) {
            gridTest = new GridTest(gridList1);
            System.out.println("仕事を割り振る: " + gridList1);
            System.out.println("gridTest: " + gridTest);
            service.submit(gridTest);
            query = "select *" +
                    "from test" +
                    "limit 10";
        }
    }

    //GridへのSocketをオープンし、request(＝仕事)を投げ、response(＝結果)を受け取る
    @Override
    public String call() throws Exception {
        Socket socket = null;
        BufferedWriter writer = null;
        BufferedReader reader = null;
        String request = this.query;

       try {
            socket = new Socket();
            // クライアントに接続
            socket.connect(this.address, 3000);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

            writer.write(request);
            writer.newLine();
            writer.flush();
            System.out.println(System.currentTimeMillis() + " " + "request to: " + this.address.getHostName() + ":" + this.address.getPort() + "[" + request + "]");
            String response = reader.readLine();
            System.out.println(System.currentTimeMillis() + " " + "response from: " + this.address.getHostName() + ":" + this.address.getPort() + " [" + response + "]");
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException e) {
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                }
                catch (IOException e) {
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                }
                catch (IOException e) {
                }
            }
        }

        //戻り値で呼び出し元の挙動に変化を及ぼすこともできる(今回は使っていない)
        return "ok";
    }
}
