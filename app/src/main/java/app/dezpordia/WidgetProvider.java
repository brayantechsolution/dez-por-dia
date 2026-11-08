package app.dezpordia;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.RemoteViews;

import java.time.LocalDate;
import java.util.List;

/** Widget da tela inicial: palavra atual + botões "Próxima" e "Meu fundo". */
public class WidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        RemoteViews views = build(context);
        for (int id : ids) manager.updateAppWidget(id, views);
    }

    /** Atualiza todos os widgets colocados na tela. */
    public static void refresh(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
        if (ids == null || ids.length == 0) return;
        RemoteViews views = build(context);
        for (int id : ids) manager.updateAppWidget(id, views);
    }

    private static RemoteViews build(Context context) {
        SharedPreferences p = Prefs.get(context);
        Prefs.ensureStart(p);
        LocalDate today = LocalDate.now();
        boolean sameDay = p.getLong(Prefs.LAST_DAY, -1) == today.toEpochDay();
        int pointer = sameDay ? p.getInt(Prefs.POINTER, 0) : 0;
        int index = pointer > 0 ? (pointer - 1) % Words.PER_DAY : 0;

        // a palavra que está na tela agora foi a de número (pointer - 1) do dia
        int round = pointer > 0 ? Words.roundOf(pointer - 1) : 0;
        List<Words.Word> words = Words.forDay(context, Prefs.studyDayIndex(p, today));
        Words.Word w = words.get(index).inRound(round);
        boolean onLock = p.getBoolean(Prefs.LOCK_SET, false);

        RemoteViews v = new RemoteViews(context.getPackageName(), R.layout.widget);
        v.setTextViewText(R.id.w_chip, pointer > 0
                ? (round > 0 ? "REVISÃO " : "PALAVRA ") + (index + 1) + " DE " + Words.PER_DAY
                : "TOQUE EM PRÓXIMA PARA COMEÇAR");
        v.setTextViewText(R.id.w_emoji, w.emoji);
        v.setViewVisibility(R.id.w_emoji, w.emoji.isEmpty() ? View.GONE : View.VISIBLE);
        v.setTextViewText(R.id.w_word, w.en);
        v.setTextViewText(R.id.w_pt, w.pt);
        v.setTextViewText(R.id.w_sentence, w.sentenceEn);
        v.setTextViewText(R.id.w_sentence_pt, w.sentencePt);
        v.setTextViewText(R.id.w_status, onLock
                ? "Na sua tela de bloqueio agora"
                : "Seu papel de parede está de volta");

        v.setOnClickPendingIntent(R.id.w_next, action(context, ActionReceiver.ACTION_NEXT, 20));
        v.setOnClickPendingIntent(R.id.w_mine, action(context, ActionReceiver.ACTION_SEEN, 21));

        Intent open = new Intent(context, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        v.setOnClickPendingIntent(R.id.w_root, PendingIntent.getActivity(context, 22, open,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));
        return v;
    }

    private static PendingIntent action(Context context, String action, int code) {
        Intent i = new Intent(context, ActionReceiver.class).setAction(action);
        return PendingIntent.getBroadcast(context, code, i,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
