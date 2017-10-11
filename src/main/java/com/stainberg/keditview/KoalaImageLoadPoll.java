package com.stainberg.keditview;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Stainberg on 7/6/17.
 */

class KoalaImageLoadPoll {

    private static KoalaImageLoadPoll instance;
    private ExecutorService poll;

    private KoalaImageLoadPoll() {
        poll = Executors.newCachedThreadPool();
    }

    public static KoalaImageLoadPoll getPoll() {
        if(instance == null) {
            synchronized (KoalaImageLoadPoll.class) {
                if(instance == null) {
                    instance = new KoalaImageLoadPoll();
                }
            }
        }
        return instance;
    }

    public void handle(Runnable r) {
        poll.execute(r);
    }

}
