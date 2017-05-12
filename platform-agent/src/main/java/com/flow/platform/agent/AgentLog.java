package com.flow.platform.agent;

/**
 * Created by gy@fir.im on 12/05/2017.
 *
 * @copyright fir.im
 */
public class AgentLog {

    static void info(String message) {
        System.out.println(message);
    }

    static void err(Throwable e, String description) {
        System.out.println(description);
        System.out.println(e.getMessage());
    }
}
