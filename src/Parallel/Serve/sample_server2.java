package Parallel.Serve;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class sample_server2 {
    //PORT number
    public static final int PORT = 10000; //待受ポート番号

    public static void main(String[] args) {
        sample_server2 sm = new sample_server2();

        try {
            ServerSocket ss = new ServerSocket(PORT);

            System.out.println("Waiting now ...");

            while (true) {
                try {
                    //サーバ側ソケット作成
                    Socket sc = ss.accept();
                    System.out.println("Welcome!");
                    ConnectToClient cc = new ConnectToClient(sc);
                    cc.start();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


class ConnectToClient extends Thread {
    private Socket sc;
    private BufferedReader br;
    private PrintWriter pw;

    //コンストラクタ
    public ConnectToClient(Socket s) {
        sc = s;
    }

    //スレッド実行
    public void run() {
        try {
            //クライアントから送られてきたデータを一時保存するバッファ
            br = new BufferedReader(new InputStreamReader(sc.getInputStream()));
            //サーバがクライアントへ送るデータを一時保存するバッファ
            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sc.getOutputStream())));
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                //受信バッファからデータを読み込む
                String str = br.readLine();
                System.out.println(str);
                Random rnd = new Random();
                RandomStrings rs = new RandomStrings();
                //クライアントからのメッセージの語尾＋ランダムな文字配列を送信バッファへ渡す
                pw.println("Server : [" + str.charAt(str.length()-1) + rs.GetRandomString(rnd.nextInt(10)) + "] !");
                //ここ重要！flushメソッドを呼ぶことでソケットを通じてデータを送信する
                pw.flush();
            } catch (IOException e) {
                try {
                    br.close();
                    pw.close();
                    sc.close();
                    System.out.println("Good Bye !!");
                    break;
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }
}

class RandomStrings {
    private final String stringchar = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private Random rnd = new Random();
    private StringBuffer sbf = new StringBuffer(15);

    public String GetRandomString(int cnt) {
        for (int i = 0; i < cnt; i++) {
            int val = rnd.nextInt(stringchar.length());
            sbf.append(stringchar.charAt(val));
        }

        return sbf.toString();
    }
}