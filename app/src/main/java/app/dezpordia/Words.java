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
        public final String emoji;
        /** Segunda frase, usada quando as 10 palavras do dia começam a repetir. */
        public final String sentenceEn2;
        public final String sentencePt2;

        Word(String en, String pt, String sentenceEn, String sentencePt, String review, String emoji,
             String sentenceEn2, String sentencePt2) {
            this.en = en;
            this.pt = pt;
            this.sentenceEn = sentenceEn;
            this.sentencePt = sentencePt;
            this.review = review;
            this.emoji = emoji;
            this.sentenceEn2 = sentenceEn2;
            this.sentencePt2 = sentencePt2;
        }

        /**
         * A palavra na volta {@code round} do dia: 0 = frase principal,
         * 1 = segunda frase, 2 = principal de novo, e assim por diante.
         */
        public Word inRound(int round) {
            if (round % 2 == 0 || sentenceEn2.isEmpty()) return this;
            return new Word(en, pt, sentenceEn2, sentencePt2, "", emoji, sentenceEn, sentencePt);
        }
    }

    /** Volta do dia a partir de quantas palavras já foram mostradas antes desta. */
    public static int roundOf(int shownBefore) {
        return Math.max(0, shownBefore) / PER_DAY;
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
                        o.optString("r", ""),
                        o.optString("e", ""),
                        o.optString("s2", ""),
                        o.optString("t2", "")));
            }
        } catch (Exception e) {
            list.add(new Word("hello", "olá", "Hello, how are you?", "Olá, tudo bem?", "", "👋",
                    "Say hello to your friends.", "Diga oi para os seus amigos."));
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
