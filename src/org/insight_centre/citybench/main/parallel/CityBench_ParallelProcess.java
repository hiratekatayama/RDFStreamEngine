package org.insight_centre.citybench.main.parallel;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import Parallel.Serve.Server;
import cqelsplus.engine.ExecContextFactory;
import cqelsplus.execplan.oprouters.QueryRouter;
import eu.larkc.csparql.engine.CsparqlEngineImpl;
import it.unimi.dsi.fastutil.Hash;
import org.deri.cqels.engine.CQELSEngine;
import org.deri.cqels.engine.ContinuousSelect;
import org.deri.cqels.engine.ExecContext;
import org.insight_centre.aceis.eventmodel.EventDeclaration;
import org.insight_centre.aceis.io.EventRepository;
import org.insight_centre.aceis.io.rdf.RDFFileManager;
import org.insight_centre.aceis.io.streams.cqels.*;
import org.insight_centre.aceis.observations.SensorObservation;
import org.insight_centre.aceis.utils.test.PerformanceMonitor;
import org.insight_centre.citybench.main.CityBench;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CityBench_ParallelProcess implements Callable<String> {
    //ポート番号の変数
    private String name = null;
    private Socket socket = null;

    //Citybenchの変数
    public enum RSPEngine {
        cqels, csparql, cqelsplus
    }

    public static ExecContext cqelsContext, tempContext;
    public static CsparqlEngineImpl csparqlEngine;
    public static CQELSEngine cqelsEngine;
    private static final Logger logger = LoggerFactory.getLogger(CityBench_ParallelProcess.class);
    public static ConcurrentHashMap<String, SensorObservation> obMap = new ConcurrentHashMap<String, SensorObservation>();
    public static cqelsplus.engine.ExecContext cqelsplusContext, tempplusContext;

    private InetSocketAddress address = null;
    private static String[] Args;
    private Properties prop;


    //クエリ実行
    public CityBench_ParallelProcess(String name, Socket socket, Properties prop, HashMap<String, String> parameters) throws Exception {
        //ポート番号
        this.name = name;
        this.socket = socket;
        this.prop = prop;
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

            //仕事を受け取る(クエリ)
            request = reader.readLine();
            System.out.println(System.currentTimeMillis() + " " + this.name + ": grid process request received [" + request + "]");

            //クエリをRDFストリーム処理システムで実行
            //parse property
            try {
                this.dataset = prop.getProperty("dataset");
                System.out.println(this.dataset);
                this.ontology = prop.getProperty("ontology");
                System.out.println(this.ontology);
                this.cqels_query = prop.getProperty("cqels_query");
                this.csparql_query = prop.getProperty("csparql_query");
                this.streams = prop.getProperty("streams");
                if (this.dataset == null || this.ontology == null || this.cqels_query == null || this.csparql_query == null || this.streams == null) {
                    throw new Exception("Configuration properties incomplete.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }

            // parse parameters

            //何らかの処理を実行する(CityBench)
            this.doSomething();

            //結果を返す(パフォーマンス結果)
            response = "query: " + request + "is finished";
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

    //パラメータ
    private String dataset, ontology, cqels_query, csparql_query, streams;
    private long duration = 0;
    private RSPEngine engine;
    EventRepository er;
    private double frequency = 1.0;
    public static PerformanceMonitor pm;
    private List<String> queries;
    int queryDuplicates = 1;
    private Map<String, String> queryMap = new HashMap<String, String>();

    private double rate = 1.0;
    public static ConcurrentHashMap<String, Object> registeredQueries = new ConcurrentHashMap<>();
    public static List startedStreamObjects = new ArrayList();
    private String resultName;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private Date start, end;
    private Set<String> startedStreams = new HashSet<>();


    public void deployQuery(String qid, String query) {
        try {
            if (this.csparqlEngine != null) {
                this.startCSPARQLStreamsFromQuery(query);
                this.registerCSPARQLQuery(qid, query);
            } else if (this.cqelsEngine != null) {
                this.startCQELSStramsFromQuery(query);
                this.registerCQELSQuery(qid, query);
            } else {
                this.startCQELSPLUSStramsFromQuery(query);
                this.registerCQELSQuery(qid, query);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //クエリの読み込み
    List<String> getStreamFileNamesFromQuery(String query) throws Exception {
        Set<String> resultSet = new HashSet<>();
        String[] streamSegments = query.trim().split("stream");
        if (streamSegments.length == 1)
            throw new Exception("Error parsing query, no stream statements found for: " + query);
        else {
            for (int i = 1; i < streamSegments.length; i++) {
                int indexOfLeftBracket = streamSegments[i].trim().indexOf("<");
                int indexOfRightBracket = streamSegments[i].trim().indexOf(">");
                String streamURI = streamSegments[i].substring(indexOfLeftBracket + 2, indexOfRightBracket + 1);
                logger.info("Stream detected: " + streamURI);
                resultSet.add(streamURI.split("#")[1] + ".stream");
            }
        }

        List<String> results = new ArrayList<>();
        results.addAll(resultSet);
        return results;
    }

    private List<String> loadQueries() throws Exception {
        String qd = null;
        if (this.engine == RSPEngine.cqels || this.engine == RSPEngine.cqelsplus)
            qd = this.cqels_query;
        else if (this.engine == RSPEngine.csparql)
            qd = this.csparql_query;

        if (this.queries == null) {
            File queryDirectory = new File(qd);
            if (!queryDirectory.exists())
                throw new Exception("Query directory not exist. " + qd);
            else if (!queryDirectory.isDirectory())
                throw new Exception("Query path specified is not a directory");
            else {
                File[] queryFiles = queryDirectory.listFiles();
                if (queryFiles != null) {
                    for (File queryFile : queryFiles) {
                        String qid = queryFile.getName().split("\\.")[0];
                        String qStr = new String(Files.readAllBytes(Paths.get(queryDirectory
                        + File.separator + queryFile.getName())));
                        if (this.engine != null && this.engine == RSPEngine.csparql)
                            qStr = "REGISTER QUERY " + qid + " AS " + qStr;
                        this.queryMap.put(qid, qStr);
                    }
                } else
                    throw new Exception("Cannot find query files.");
            }
        } else {
            for (String qid : this.queries) {
                try {
                    File queryFile = new File(qd + File.separator + qid);
                    String qStr = new String(Files.readAllBytes(queryFile.toPath()));
                    qid = qid.split("\\.")[0];
                    if (this.engine != null && this.engine == RSPEngine.csparql)
                        qStr = "REGISTER QUERY " + qid + " AS " + qStr;
                    this.queryMap.put(qid, qStr);
                } catch (Exception e) {
                    logger.error("Could not load query file.");
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    protected void startTest() throws Exception {
        this.loadQueries();
        pm = new PerformanceMonitor(queryMap, duration, queryDuplicates, resultName);
        System.out.println("PM: " + pm);
        new Thread(pm).start();
        if (this.engine == RSPEngine.cqels)
            this.initCQELS();
        else if (this.engine == RSPEngine.csparql)
            this.initCSPARQL();
        else if (this.engine == RSPEngine.cqelsplus)
            this.initCQELSPLUS();
    }


    //CQELSPLUS
    private void initCQELSPLUS() throws Exception {
        cqelsplusContext = tempplusContext;
        ExecContextFactory.setExecContext(cqelsplusContext);
        System.out.println("initCQELSplus start");

        for (int i = 0; i < this.queryDuplicates; i++) {
            this.registerCQELSPLUSQueries();
        }
        this.startCQELSPLUSStrams();
    }

    private void registerCQELSPLUSQueries() {
        for (Map.Entry en : this.queryMap.entrySet()) {
            String qid = en.getKey() + "-" + UUID.randomUUID();
            String query = en.getValue() + "";
            registerCQELSPLUSQuery(qid, query);
        }
    }

    private void registerCQELSPLUSQuery(String qid, String query) {
        if (!registeredQueries.keySet().contains(qid)) {
            CQELSplusResultListener crl = new CQELSplusResultListener(qid);
            logger.info("Registering result observer: " + crl.getUri());
            QueryRouter cs = cqelsplusContext.engine().registerSelectQuery(query);
            cs.addListener(crl);
            registeredQueries.put(qid, crl);
        }
    }

    private void startCQELSPLUSStrams() throws Exception {
        for (String s : this.queryMap.values()) {
            this.startCQELSPLUSStramsFromQuery(s);
        }
    }

    private void startCQELSPLUSStramsFromQuery(String query) throws Exception {
        System.out.println("start CQELSplus Streams From Query");
        List<String> streamNames = this.getStreamFileNamesFromQuery(query);
        for (String sn : streamNames) {
            System.out.println("CityBench check streamNames: " + sn);
            String uri = RDFFileManager.defaultPrefix + sn.split("\\.")[0];
            String path = this.streams + "/" + sn;
            if (!this.startedStreams.contains(uri)) {
                this.startedStreams.add(uri);
                CQELSplusSensorStream css;
                EventDeclaration ed = er.getEds().get(uri);
                if (ed == null)
                    throw new Exception("ED not found for: " + uri);
                if (ed.getEventType().contains("traffic")) {
                    System.out.println("eventType is traffic " + path);
                    css = new CQELSplusAarhusTrafficStream(cqelsplusContext, uri, path, ed, start, end);
                } else if (ed.getEventType().contains("pollution")) {
                    System.out.println("eventType is pollution " + path);
                    css = new CQELSplusAarhusPollutionStream(cqelsplusContext, uri, path, ed, start, end);
                } else if (ed.getEventType().contains("weather")) {
                    System.out.println("eventType is weather " + path);
                    css = new CQELSplusAarhusWeatherStream(cqelsplusContext, uri, path, ed, start, end);
                } else if (ed.getEventType().contains("location")) {
                    System.out.println("eventType is location " + path);
                    css = new CQELSplusLocationStream(cqelsplusContext, uri, path, ed);
                } else if (ed.getEventType().contains("parking")) {
                    System.out.println("eventType is parking " + path);
                    css =new CQELSplusAarhusParkingStream(cqelsplusContext, uri, path, ed, start, end);
                } else
                    throw new Exception("Sensor type not supported: " + ed.getEventType());

                System.out.println("Thread start");
                new Thread(css).start();
                startedStreamObjects.add(css);
            }
        }
    }

    //CQELS
    private void registerCQELSQuery(String qid, String query) {
        if (!registeredQueries.keySet().contains(qid)) {
            CQELSResultListener crl = new CQELSResultListener(qid);
            logger.info("Registering result observer: " + crl.getUri());
            ContinuousSelect cs = cqelsContext.registerSelect(query);
            cs.register(crl);
            registeredQueries.put(qid, crl);
        }
    }

    private void initCQELS() {
    }

    private void startCQELSStramsFromQuery(String query) {
    }

    //CSPARQL
    private void registerCSPARQLQuery(String qid, String query) {
    }

    private void initCSPARQL() {
    }

    private void startCSPARQLStreamsFromQuery(String query) {
    }

    //テスト
    private void doSomething() {
        //時間がかかる処理であると仮定する
        try {
            Thread.sleep(3000);
        }
        catch (InterruptedException e) {
        }
    }
}
