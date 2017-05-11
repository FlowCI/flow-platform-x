package com.flow.platform.cmd;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by gy@fir.im on 12/05/2017.
 *
 * @copyright fir.im
 */
public class CmdExecutor {

    private final CountDownLatch waitLock = new CountDownLatch(1);
    private final Queue<String> loggingQueue = new LinkedList<>();
    private final AtomicInteger loggingQueueSize = new AtomicInteger(0);
    private final AtomicBoolean isLoggingFinish = new AtomicBoolean(false);
    private final int bufferSize = 1024 * 1024 * 10;

    private ProcessBuilder pBuilder;

    public CmdExecutor(String ...cmd) {
        pBuilder = new ProcessBuilder(cmd);
        pBuilder.redirectErrorStream(true);
    }

    public CmdResult run() {
        long startTime = System.currentTimeMillis();
        CmdResult resultObj = new CmdResult();

        try {
            Process p = pBuilder.start();
            resultObj.setPid(getPid(p));

            // start thread to read logging stream
            Thread threadForStream = new Thread(createCmdStreamReader(p));

            // start thread to make dequeue operation
            Thread threadForLogging = new Thread(createCmdLoggingReader());

            threadForStream.start();
            threadForLogging.start();

            resultObj.setExitValue(p.waitFor());
            System.out.println(" ===== Process executed =====");

            waitLock.await();
            System.out.println(" ===== Logging executed =====");

        } catch (InterruptedException | IOException e) {
            resultObj.getExceptions().add(e);
        } finally {
            // calculate duration
            long endTime = System.currentTimeMillis();
            long durationInSecond = (endTime - startTime) / 1000;
            resultObj.setDuration(durationInSecond);

            return resultObj;
        }
    }

    private int getPid(Process process) {
        try {
            Class<?> cProcessImpl = process.getClass();
            Field fPid = cProcessImpl.getDeclaredField("pid");
            if (!fPid.isAccessible()) {
                fPid.setAccessible(true);
            }
            return fPid.getInt(process);
        } catch (Exception e) {
            return -1;
        }
    }

    private Runnable createCmdLoggingReader() {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        if (isLoggingFinish.get() && loggingQueueSize.get() <= 0) {
                            break;
                        }

                        String line = loggingQueue.poll();
                        if (line == null) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ignored) {
                            }
                        } else {
                            System.out.println(line);
                            loggingQueueSize.getAndDecrement();
                        }
                    }
                } finally {
                    waitLock.countDown();
                }
            }
        };
    }

    private Runnable createCmdStreamReader(Process p) {
        return new CmdStreamReader(p.getInputStream(), bufferSize, new CmdStreamReader.CmdStreamListener() {
            @Override
            public void onLogging(String line) {
                loggingQueue.add(line);
                loggingQueueSize.getAndIncrement();
            }

            @Override
            public void onFinish() {
                isLoggingFinish.set(true);
            }
        });
    }
}
