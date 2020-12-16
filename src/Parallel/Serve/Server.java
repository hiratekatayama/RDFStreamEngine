package Parallel.Serve;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(6666);
            System.out.println("server start...");
            Socket s = serverSocket.accept();
            //ログ出力
            System.out.println("client: " + s.getInetAddress().getLocalHost() + "access successed");

            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            //クライアントからのデータ情報を読み込む
            String test = br.readLine();
            System.out.println("readLine: " + test);

            String mess = "select *" +
                    "from test";

            //ログ出力
            System.out.println("cliend: " + mess);

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

            //クライアントへデータ情報を書き出す
            bw.write(mess + "\n");
            bw.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}