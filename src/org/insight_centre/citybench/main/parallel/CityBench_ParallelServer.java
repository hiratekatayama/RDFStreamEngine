package org.insight_centre.citybench.main.parallel;

import Parallel.Grid;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CityBench_ParallelServer {
    public static void main(String[] args) throws Exception {
        /*
            引数1: Grid名(任意の名前でよい)
            引数2: 使用するTCPポート番号
         */
        CityBench_ParallelServer grid = new CityBench_ParallelServer(args[0], Integer.parseInt(args[1]));
        grid.start();
    }

    private String name = null;
    private int port = -1;

    public CityBench_ParallelServer(String name, int port) {
        this.name = name;
        this.port = port;
    }

    //メインスレッドでSocketのコネクションが到達するまで待機する
    public void start() throws Exception {
        System.out.println(System.currentTimeMillis() + " " + this.name + ": grid started");

        ServerSocket server = null;
        Socket socket;

        //GridProcess(実際に仕事をこなす子スレッド)の最大同時起動数
        ExecutorService service = Executors.newFixedThreadPool(3);
        CityBench_ParallelProcess process;
        int counter = 0;

        try {
            server = new ServerSocket(this.port);
            while (true) {
                while (true) {
                    //Socketを受け付けるとループを抜け、GridProcessに引き渡される
                    try {
                        socket = server.accept();
                        socket.setSoTimeout(30000);
                        break;
                    } catch (IOException e) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e2) {
                        }
                    }
                }

                //最大同時起動数に達するまで子スレッドが作られ、並行して仕事を実行する
                process = new CityBench_ParallelProcess(this.name + "_" + ++counter, socket);
                service.submit(process);
            }
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                }
            }
        }
    }
}