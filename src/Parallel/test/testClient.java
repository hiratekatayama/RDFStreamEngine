package Parallel.test;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class testClient {
    public static void main(String[] args) {
        try {
            Socket s = new Socket("127.0.0.1", 6666);

            //ストリームのインスタンスを取る
            InputStream input_stream = s.getInputStream();
            OutputStream output_stream = s.getOutputStream();

            BufferedWriter buffered_writer = new BufferedWriter(new OutputStreamWriter(output_stream));

            //サーバへのデータ送信書き込み
            buffered_writer.write("cliend -> server:hello server! \n");
            //サーバへデータ情報を送信
            buffered_writer.flush();

            //サーバからの返信を読み込み
            BufferedReader buffered_reader = new BufferedReader(new InputStreamReader(input_stream));
            String mess = buffered_reader.readLine();

            //ログ出力
            System.out.println("Server: " + mess);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
