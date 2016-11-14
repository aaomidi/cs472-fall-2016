package com.aaomidi.ftpserver.engine;

import java.util.Timer;
import java.util.TimerTask;

public class Blacklist {
    private int count;

    public Blacklist() {
        this.count = 0;
        this.timer();
    }

    public void addCount() {
        count += 1;
    }

    public boolean canWork() {
        if (count >= 3) {
            return false;
        }
        return true;
    }

    public void timer() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                --count;
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 30000, 30000);
    }

}
