package org.insight_centre.citybench.main.parallel;

import eu.larkc.csparql.engine.CsparqlEngineImpl;
import it.unimi.dsi.fastutil.Hash;
import org.deri.cqels.engine.CQELSEngine;
import org.deri.cqels.engine.ExecContext;
import org.insight_centre.aceis.io.EventRepository;
import org.insight_centre.aceis.observations.SensorObservation;
import org.insight_centre.aceis.utils.test.PerformanceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
//import org.apache.log4j.Logger;


public class CityBench_Parallel implements Callable<String> {

    public enum RSPEngine {
        cqels, csparql, cqelsplus
    }

    //クラス変数
    public static ExecContext cqelsContext, tempContext;
    public static CsparqlEngineImpl csparqlEngine;
    public static CQELSEngine cqelsEngine;
    public static cqelsplus.engine.ExecContext cqelsplusContext, tempplusContext;
    private static final Logger logger = LoggerFactory.getLogger(CityBench_Parallel.class);
    public static ConcurrentHashMap<String, SensorObservation> obMap = new ConcurrentHashMap<>();

    //グリッド追加
    private InetSocketAddress address = null;
    private static String[] Args;

    //クエリ
    //private static Future<String> query;
    public static String query = null;

    public static void main(String[] args) throws Exception {
        //仕事をさせる対象の（サーバ+ポート）のリスト
        InetSocketAddress[] gridList = new InetSocketAddress[]{
                new InetSocketAddress("127.0.0.1", 5100),
                new InetSocketAddress("127.0.0.1", 5101),
                new InetSocketAddress("127.0.0.1", 5102)
        };

        //Citybenchプロパティ作成
        Properties prop = new Properties();
        File in = new File("citybench.properties");
        FileInputStream fis = new FileInputStream(in);
        prop.load(fis);
        fis.close();

        HashMap<String, String> parameters = new HashMap<String, String>();
        for (String s : args) {
            parameters.put(s.split("=")[0], s.split("=")[1]);
        }
        //クエリの取得
        Load_Query_Param(parameters);
        //クエリの登録
        RoundRobin_Worker(gridList);
    }

    //クエリ変数
    public static List<String> queries;
    public static int queryDuplicates = 1;

    //コンストラクタ
    public CityBench_Parallel(InetSocketAddress address) {
        this.address = address;
    }

    //クエリ登録
    public static void Load_Query_Param(HashMap<String, String> parameters) throws Exception {
        //パラメータ取得
        //クエリ番号
        if (parameters.containsKey("query")) {
            queries = Arrays.asList(parameters.get("query").split(","));
            //System.out.println("query" + queries);
        }
        //クエリ複製数
        if (parameters.get("queryDuplicates") != null) {
            queryDuplicates = Integer.parseInt(parameters.get("queryDuplicates"));
        }
    }

    //クエリの登録方法
    protected static void RoundRobin_Worker(InetSocketAddress[] gridList) {
        ExecutorService service = Executors.newCachedThreadPool();

        int cnt = 0, gridads = 0;
        //ラウンドロビン
        for (String query_list : queries) {
            gridads = cnt % gridList.length;
            CityBench_Parallel cb = new CityBench_Parallel(gridList[gridads]);
            System.out.println("仕事を割り振る: " + gridList[gridads]);
            query = query_list;
            service.submit(cb);
            //次のインスタンスへ移動
            cnt++;
        }

    }


    //サーバへの登録条件
    @Override
    public String call() throws Exception {
        Socket socket = null;
        BufferedWriter writer = null;
        BufferedReader reader = null;
        String request = query;

        try {
            socket = new Socket();
            //クライアントに接続
            socket.connect(this.address, 3000);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            writer.write(request);
            System.out.println("server: " + request);
            writer.newLine();
            writer.flush();
            System.out.println(System.currentTimeMillis() + " " + "request to: " + this.address.getHostName() + ":" + this.address.getPort() + "[" + request + "]");
            String response = reader.readLine();
            System.out.println(System.currentTimeMillis() + " " + "response from: " + this.address.getHostName() + ":" + this.address.getPort() + "[" + response + "]");
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

        return "ok";
    }
}
