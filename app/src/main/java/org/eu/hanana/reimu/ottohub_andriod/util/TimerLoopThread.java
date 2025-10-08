package org.eu.hanana.reimu.ottohub_andriod.util;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

public class TimerLoopThread<T> extends Thread{
    protected AtomicReference<T> value;
    protected Function<T,Boolean> func;
    protected long miles;
    protected boolean working=true;
    public TimerLoopThread(AtomicReference<T> value, Function<T,Boolean> func, long miles){
        this.value=value;
        this.func=func;
        this.miles=miles;
    }

    @Override
    public void interrupt() {
        super.interrupt();
        working=false;
    }

    @Override
    public void run() {
        working=true;
        while (!Thread.currentThread().isInterrupted()&&working){
            try {
                Thread.sleep(miles);
            } catch (InterruptedException ignored) {
            }
            if (func.apply(value.get())){
                break;
            }
        }
    }
}
