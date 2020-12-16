package Parallel.test;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class sampleGrid {
    public static void main(String[] args) throws IOException {
        /**
         * 引数1：Grid名
         * 引数2：使用するTCPポート番号
         */
        sampleGrid grid = new sampleGrid(args[0], Integer.parseInt(args[1]));
        grid.start();
    }

    private String name = null;
    private int port = -1;

    public sampleGrid(String name, int port) {
        this.name = name;
        this.port = port;
    }

    public void start() throws IOException {
        System.out.println(System.currentTimeMillis() + " " + this.name + ": grid started");

        ServerSocket server  =null;
        Socket socket;

        //GridProcess
        ExecutorService service = Executors.newFixedThreadPool(2);
        sampleGridProcess process;
        int counter = 0;

        server = new ServerSocket(this.port);
        while (true) {
            while (true) {
                //Socketを受け取るとループを抜け，GridProcessに引き渡される
                try {
                    socket = server.accept();
                    socket.setSoTimeout(30000);
                    break;
                } catch (IOException e) {
                }
            }

            //最大同時期同数に達するまで小スレッドが作られ，並行して仕事を実行する
            process = new sampleGridProcess(this.name + "_" + ++counter, socket);
            service.submit(process);
        }

    }

}
