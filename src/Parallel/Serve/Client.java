package Parallel.Serve;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    public static void main(String[] args) {
        try {
            Socket s = new Socket("127.0.0.1", 6666);

            //ストリームのインスタンスを取る
            InputStream is = s.getInputStream();
            OutputStream os = s.getOutputStream();

            // System.out.println("is: " + is);
            // System.out.println("os: " + os);

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));

            //サーバへデータ情報を送信
            bw.write("cliend -> server:hello server! \n");
            bw.flush();

            //サーバからの返信を読み込む
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String mess = br.readLine();
            //ログ出力
            System.out.println("server: " + mess);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
