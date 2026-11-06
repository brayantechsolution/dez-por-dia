package app.dezpordia;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Recebe os toques nos botões "Já vi" e "Próxima" da tela de bloqueio. */
public class ActionReceiver extends BroadcastReceiver {

    public static final String ACTION_SEEN = "app.dezpordia.SEEN";
    public static final String ACTION_NEXT = "app.dezpordia.NEXT";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Context app = context.getApplicationContext();
        PendingResult result = goAsync();
        new Thread(() -> {
            try {
                if (ACTION_NEXT.equals(action)) {
                    Updater.tick(app, true);
                } else if (ACTION_SEEN.equals(action)) {
                    // Volta o papel de parede da pessoa; o app continua ativo
                    Updater.clearLock(app);
                }
                // Recomeça a contagem: a próxima palavra vem só depois do intervalo inteiro
                if (Prefs.get(app).getBoolean(Prefs.ENABLED, false)) {
                    Scheduler.schedule(app);
                }
            } finally {
                result.finish();
            }
        }).start();
    }
}
