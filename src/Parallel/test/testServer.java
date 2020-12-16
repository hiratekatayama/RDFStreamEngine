package Parallel.test;

import Parallel.Serve.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class testServer {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(6666);
            System.out.println("server start ...");
            Socket server_accept = serverSocket.accept();
            System.out.println("client: " + server_accept.getInetAddress().getLocalHost() + "access successed");

            BufferedReader buffered_reader = new BufferedReader(new InputStreamReader(server_accept.getInputStream()));

            //クライアントからのデータ情報を読み込む
            String test = buffered_reader.readLine();
            System.out.println("readLine: " + test);

            //
            String mess = "select *" +
                    "from test";

            BufferedWriter buffered_writer = new BufferedWriter(new OutputStreamWriter(server_accept.getOutputStream()));

            //クライアントへデータ情報を書き出す
            buffered_writer.write(mess + "\n");
            buffered_writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
