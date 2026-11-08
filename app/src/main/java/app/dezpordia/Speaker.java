package app.dezpordia;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.media.AudioAttributes;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Toast;

import java.util.Locale;

/**
 * Fala a palavra ou a frase em inglês com a voz do próprio celular (funciona sem internet).
 * Usado pelos botões "🔊 Palavra" e "🔊 Frase" do widget.
 */
final class Speaker {

    /** Ritmo de fala: um pouco mais devagar que o normal, para ouvir cada som. */
    private static final float RATE_NORMAL = 0.85f;
    private static final float RATE_SLOW = 0.6f;
    /** Desliga a voz depois de um tempo sem uso, para não gastar bateria. */
    private static final long IDLE_MS = 60_000;
    /** Garante que o toque nunca fique "pendurado" se a voz travar. */
    private static final long SAFETY_MS = 20_000;

    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static TextToSpeech tts;
    private static boolean ready;
    private static Runnable waiting;
    private static BroadcastReceiver.PendingResult pending;
    private static String lastId;

    private static final Runnable SHUTDOWN = () -> {
        if (tts != null) {
            tts.shutdown();
            tts = null;
            ready = false;
        }
    };

    private static final Runnable FINISH = Speaker::finishPending;

    private Speaker() {}

    /** A palavra duas vezes: no ritmo normal e depois bem devagar. */
    static void sayWord(Context app, String word, BroadcastReceiver.PendingResult result) {
        run(app, result, () -> {
            String id = newId();
            tts.setSpeechRate(RATE_NORMAL);
            tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, id + "a");
            tts.playSilentUtterance(500, TextToSpeech.QUEUE_ADD, id + "b");
            tts.setSpeechRate(RATE_SLOW);
            speakLast(word, id);
        });
    }

    /** A frase inteira, um pouco mais devagar que o normal. */
    static void saySentence(Context app, String sentence, BroadcastReceiver.PendingResult result) {
        run(app, result, () -> {
            String id = newId();
            tts.setSpeechRate(RATE_NORMAL);
            tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, id + "z");
            lastId = id + "z";
        });
    }

    private static void speakLast(String text, String id) {
        // O ritmo vale para o que é colocado na fila depois dele
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, id + "z");
        lastId = id + "z";
    }

    private static String newId() {
        return "u" + System.nanoTime();
    }

    /** Roda {@code job} com a voz pronta; cria a voz na primeira vez. Sempre na thread principal. */
    private static void run(Context app, BroadcastReceiver.PendingResult result, Runnable job) {
        MAIN.post(() -> {
            finishPending();
            pending = result;
            MAIN.postDelayed(FINISH, SAFETY_MS);
            MAIN.removeCallbacks(SHUTDOWN);
            MAIN.postDelayed(SHUTDOWN, IDLE_MS);

            if (tts != null && ready) {
                job.run();
                return;
            }
            waiting = job;
            if (tts != null) return; // ainda iniciando; roda quando ficar pronta
            tts = new TextToSpeech(app, status -> MAIN.post(() -> onInit(app, status)));
        });
    }

    private static void onInit(Context app, int status) {
        if (tts == null) return;
        if (status != TextToSpeech.SUCCESS) {
            toast(app, "Este celular não tem uma voz instalada para ler o inglês.");
            SHUTDOWN.run();
            finishPending();
            return;
        }
        int lang = tts.setLanguage(Locale.US);
        if (lang == TextToSpeech.LANG_MISSING_DATA || lang == TextToSpeech.LANG_NOT_SUPPORTED) {
            lang = tts.setLanguage(Locale.ENGLISH);
        }
        if (lang == TextToSpeech.LANG_MISSING_DATA || lang == TextToSpeech.LANG_NOT_SUPPORTED) {
            toast(app, "Falta a voz em inglês. Instale em Configurações › Idioma › Saída de texto para fala.");
            SHUTDOWN.run();
            finishPending();
            return;
        }
        tts.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build());
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String id) {}
            @Override public void onDone(String id) { if (id.equals(lastId)) MAIN.post(FINISH); }
            @Override public void onError(String id) { MAIN.post(FINISH); }
            @Override public void onStop(String id, boolean interrupted) {}
        });
        ready = true;
        Runnable job = waiting;
        waiting = null;
        if (job != null) job.run();
    }

    private static void finishPending() {
        MAIN.removeCallbacks(FINISH);
        if (pending != null) {
            try {
                pending.finish();
            } catch (IllegalStateException ignored) {
                // já encerrado
            }
            pending = null;
        }
    }

    private static void toast(Context app, String msg) {
        Toast.makeText(app, msg, Toast.LENGTH_LONG).show();
    }

}
