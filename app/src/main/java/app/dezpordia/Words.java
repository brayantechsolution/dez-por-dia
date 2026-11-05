package app.dezpordia;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Banco de palavras (assets/words.json) e as 10 palavras de cada dia. */
public final class Words {

    public static final int PER_DAY = 10;

    public static final class Word {
        public final String en;
        public final String pt;
        public final String sentenceEn;
        public final String sentencePt;
        public final String review;

        Word(String en, String pt, String sentenceEn, String sentencePt, String review) {
            this.en = en;
            this.pt = pt;
            this.sentenceEn = sentenceEn;
            this.sentencePt = sentencePt;
            this.review = review;
        }
    }

    private static List<Word> cache;

    private Words() {}

    public static synchronized List<Word> all(Context context) {
        if (cache != null) return cache;
        List<Word> list = new ArrayList<>();
        try (InputStream in = context.getAssets().open("words.json")) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            JSONArray arr = new JSONArray(new String(out.toByteArray(), StandardCharsets.UTF_8));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new Word(
                        o.getString("w"),
                        o.getString("pt"),
                        o.getString("s"),
                        o.getString("t"),
                        o.optString("r", "")));
            }
        } catch (Exception e) {
            list.add(new Word("hello", "olá", "Hello, how are you?", "Olá, tudo bem?", ""));
        }
        cache = Collections.unmodifiableList(list);
        return cache;
    }

    /** As 10 palavras do dia de estudo número {@code dayIndex} (0 = primeiro dia). */
    public static List<Word> forDay(Context context, int dayIndex) {
        List<Word> all = all(context);
        List<Word> day = new ArrayList<>(PER_DAY);
        for (int i = 0; i < PER_DAY; i++) {
            day.add(all.get((dayIndex * PER_DAY + i) % all.size()));
        }
        return day;
    }
}
