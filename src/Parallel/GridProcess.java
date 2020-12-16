package Parallel;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Callable;


public class GridProcess implements Callable<String> {

    private String name = null;
    private Socket socket = null;

    public GridProcess(String name, Socket socket) {
        this.name = name;
        this.socket = socket;
    }

    @Override
    public String call() throws Exception {
        System.out.println(System.currentTimeMillis() + " " + this.name + ": grid process started");

        BufferedReader reader = null;
        BufferedWriter writer = null;
        String request;
        String response;

        try {
            reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
            writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream(), "UTF-8"));

            //仕事を受け取る
            request = reader.readLine();
            System.out.println(System.currentTimeMillis() + " " + this.name + ": grid process request received [" + request + "]");

            //何らかの処理を実行する
            this.doSomething();

            //結果を返す
            response = "Hello, my name is " + this.name;
            writer.write(response + "\n");
            writer.flush();
            System.out.println(System.currentTimeMillis() + " " + this.name + ": grid process response returned [" + response + "]");
        }
        finally {
            if (writer != null) {
                try {
                    writer.close();
                }
                catch (IOException e) {
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException e) {
                }
            }
        }

        //戻り値で呼び出し元の挙動に変化を及ぼすこともできる(今回は使っていない)
        return "ok";
    }

    private void doSomething() {
        //時間がかかる処理であると仮定する
        try {
            Thread.sleep(3000);
        }
        catch (InterruptedException e) {
        }
    }
}
