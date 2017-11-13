package com.stainberg.keditview

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by Stainberg on 7/6/17.
 */

internal class KoalaImageLoadPoll private constructor() {
    private val poll : ExecutorService

    init {
        poll = Executors.newCachedThreadPool()
    }

    fun handle(r : Runnable) {
        poll.execute(r)
    }

    companion object {

        private var instance : KoalaImageLoadPoll? = null

        fun getPoll() : KoalaImageLoadPoll {
            if (instance == null) {
                synchronized(KoalaImageLoadPoll::class.java) {
                    if (instance == null) {
                        instance = KoalaImageLoadPoll()
                    }
                }
            }
            return instance!!
        }
    }

}
