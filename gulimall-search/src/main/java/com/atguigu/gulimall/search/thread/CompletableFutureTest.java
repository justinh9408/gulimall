package com.atguigu.gulimall.search.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author: Justin
 */
public class CompletableFutureTest {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<Void> f1 = CompletableFuture.runAsync(() -> {
            System.out.println("01");
        });

        CompletableFuture<Void> f2 = CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(2000);
                System.out.println("02");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        CompletableFuture<Object> allOf = CompletableFuture.anyOf(f1, f2);
        allOf.get();
    }
}
