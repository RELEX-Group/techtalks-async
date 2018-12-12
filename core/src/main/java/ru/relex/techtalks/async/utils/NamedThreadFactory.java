package ru.relex.techtalks.async.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
    private String name;
    private AtomicInteger threadNo = new AtomicInteger(0);

    public NamedThreadFactory(String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable target) {
        return new Thread(target, name + "-" + threadNo.getAndIncrement());
    }
}