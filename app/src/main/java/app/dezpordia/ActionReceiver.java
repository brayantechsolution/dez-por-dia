package app.dezpordia;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Recebe os toques nos botões da tela de bloqueio e do widget. */
public class ActionReceiver extends BroadcastReceiver {

    public static final String ACTION_SEEN = "app.dezpordia.SEEN";
    public static final String ACTION_NEXT = "app.dezpordia.NEXT";
    /** Botões de áudio do widget: falam a palavra ou a frase que está na tela. */
    public static final String ACTION_SAY_WORD = "app.dezpordia.SAY_WORD";
    public static final String ACTION_SAY_SENTENCE = "app.dezpordia.SAY_SENTENCE";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Context app = context.getApplicationContext();
        if (ACTION_SAY_WORD.equals(action) || ACTION_SAY_SENTENCE.equals(action)) {
            Words.Word w = WidgetProvider.current(app);
            PendingResult spoken = goAsync();
            if (ACTION_SAY_WORD.equals(action)) Speaker.sayWord(app, w.en, w.past, spoken);
            else Speaker.saySentence(app, w.sentenceEn, spoken);
            return;
        }
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
