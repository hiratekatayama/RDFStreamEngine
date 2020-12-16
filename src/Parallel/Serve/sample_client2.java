package Parallel.Serve;

/**
 * 1. ソケットの生成
 * 2. スレッドクラスのインスタンスを生成し実行
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class sample_client2 extends JFrame implements Runnable {
    //接続先ホスト名
    public static final String HOST = "localhost";

    //接続先ポート番号
    // ポート番号はクライアントとサーバ間で一致させる必要がある
    public static final int PORT = 10000;

    //GUIアプリを作ったので，ソケット通信に関係がないメンバ変数が含まれている
    private JTextField tf;
    private JTextArea ta;
    private JScrollPane sp;
    private JPanel pn;
    private JButton bt;

    //ソケット通信用の変数です．
    private Socket sc;
    private BufferedReader br;
    private PrintWriter pw;

    public static void main(String[] args) {
        sample_client2 cl = new sample_client2();
    }

    public sample_client2() {

        tf = new JTextField();
        ta = new JTextArea();
        sp = new JScrollPane(ta);
        pn = new JPanel();
        bt = new JButton("Send");

        //GUI冷暗と
        pn.add(bt);
        add(tf, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(pn, BorderLayout.SOUTH);

        bt.addActionListener(new SampleActionListener());
        addWindowListener(new SampleWindowListener());

        setSize(400, 300);
        setVisible(true);

        //Threadクラスのインスタンスを作成・実行
        //個々でソケット通信用のスレッドが作成され，通信が開始
        Thread th = new Thread(this);
        th.start();
    }

    public void run() {
        try {
            //個々でサーバへ接続
            sc = new Socket(HOST, PORT);
            br = new BufferedReader(new InputStreamReader(sc.getInputStream()));
            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sc.getOutputStream())));

            //サーバから受け取ったデータをGUI表示させる
            while (true) {
                try {
                    String str = br.readLine();
                    ta.append(str + "\n");
                } catch (Exception e) {
                    br.close();
                    pw.close();
                    sc.close();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class SampleActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try{
                //GUI アプリ
                String str = tf.getText();
                pw.println(str);
                ta.append(str + "\n");
                pw.flush();
                tf.setText("");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public class SampleWindowListener extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            System.exit(0);
        }
    }
}
