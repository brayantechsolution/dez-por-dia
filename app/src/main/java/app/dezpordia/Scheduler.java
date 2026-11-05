package app.dezpordia;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

/** Agenda a troca periódica do fundo da tela de bloqueio. */
public final class Scheduler {

    private static final int JOB_ID = 10;

    private Scheduler() {}

    public static void schedule(Context context) {
        int hours = Prefs.get(context).getInt(Prefs.INTERVAL_HOURS, 2);
        long interval = hours * 60L * 60L * 1000L;
        JobInfo job = new JobInfo.Builder(JOB_ID, new ComponentName(context, WallpaperJob.class))
                .setPeriodic(interval, 15L * 60L * 1000L)
                .setPersisted(true)
                .build();
        JobScheduler js = context.getSystemService(JobScheduler.class);
        if (js != null) js.schedule(job);
    }

    public static void cancel(Context context) {
        JobScheduler js = context.getSystemService(JobScheduler.class);
        if (js != null) js.cancel(JOB_ID);
    }
}
