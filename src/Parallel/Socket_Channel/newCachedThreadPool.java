package Parallel.Socket_Channel;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class newCachedThreadPool {
    public static void main(String[] args) {
        System.out.println("CallableSample Start");
        //1.Executorクラスの生成：引数にスレッド数を渡す
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        for (int i = 0; i < 10; i++) {
            //2.Callableクラスのcallメソッドを実装して，Taskリストに登録
            executorService.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    Date now = new Date();
                    System.out.println(now + " [THREAD_ID] " + Thread.currentThread().getId());
                    Thread.sleep(2000);
                    return true;
                }
            });
        }
        //3.サービス終了
        executorService.shutdown();
        System.out.println("CallableSample end.");
        return;
    }
}
