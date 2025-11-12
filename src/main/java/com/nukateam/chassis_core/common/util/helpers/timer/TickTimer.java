package com.nukateam.chassis_core.common.util.helpers.timer;

import com.nukateam.chassis_core.common.util.helpers.timer.PlayOnceTimerTask;
import com.nukateam.chassis_core.common.util.helpers.timer.TimerTask;

import java.util.ArrayList;

public class TickTimer {
    private final ArrayList<com.nukateam.chassis_core.common.util.helpers.timer.TimerTask> tasks = new ArrayList<>();

    public void tick() {
        ArrayList<com.nukateam.chassis_core.common.util.helpers.timer.TimerTask> removed = new ArrayList<>();

        for (com.nukateam.chassis_core.common.util.helpers.timer.TimerTask task : tasks) {
            task.tick();
            if (task.isCompleted())
                removed.add(task);
        }

        tasks.removeAll(removed);
    }

    public void addTimer(TimerTask task) {
        tasks.add(task);
    }

    public void addCooldownTimer(int duration, Runnable predicate) {
        tasks.add(new PlayOnceTimerTask(duration, predicate));
    }

}

