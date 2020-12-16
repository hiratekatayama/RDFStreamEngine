package Parallel.Socket_Channel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

public class newCachedThreadPool2 {
    public static void main(String[] args) {
        System.out.println("CallableSample start.");

        // Executorクラス生成：引数にスレッドを渡す
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        //各スレッドの処理を持つためのTaskリストを用意
        List<Future<Boolean>> submitTaskList = new ArrayList<Future<Boolean>>();

        for (int i = 0; i < 10; i++) {
            //2.Callableクラスのcallメソッドを実装して,Taskリストに登録
            submitTaskList.add(executorService.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    Date now = new Date();
                    System.out.println(now + "[THREAD_ID]" + Thread.currentThread().getId());
                    Thread.sleep(2000);
                    return true;
                }
            }));
        }

        //3.スレッド処理を待つ
        try {
            for (Future<Boolean> future: submitTaskList) {
                if (future.get() == false) {
                    throw  new RuntimeException("スレッド処理エラー");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        //4.実行サービス終了
        executorService.shutdown();
        System.out.println("CallableSampl end.");
        return;
    }
}
