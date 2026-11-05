package app.dezpordia;

import android.app.job.JobParameters;
import android.app.job.JobService;

/** Roda em segundo plano, de tempos em tempos, e troca a frase do fundo. */
public class WallpaperJob extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        new Thread(() -> {
            Updater.tick(getApplicationContext(), false);
            jobFinished(params, false);
        }).start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
