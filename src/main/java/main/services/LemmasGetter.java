package main.services;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LemmasGetter {

    private static final String[] AUXILIARY_PARTS_OF_SPEECH = {"ПРЕДЛ", "МЕЖД", "СОЮЗ", "МС", "ЧАСТ"};
    private static LuceneMorphology morphology;

    static {
        try {
            morphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static Map<String, Integer> getLemmas(String text) {
        Map<String, Integer> lemmas = new ConcurrentHashMap<>();

        if(!text.isEmpty()) {
            String[] words = text.toLowerCase().replaceAll("-", " ")
                    .replaceAll("[^А-Яа-я\\s]", "")
                    .trim().split("\\s+");

            for (String word : words) {
                if (word.isEmpty()) {
                    continue;
                }
                if (isAuxiliaryPartOfSpeech(morphology, word) || word.length() == 1) {
                    continue;
                }

                String lemma = morphology.getNormalForms(word).get(0);

                if (lemmas.containsKey(lemma)) {
                    lemmas.put(lemma, lemmas.get(lemma) + 1);
                } else {
                    lemmas.put(lemma, 1);
                }
            }
        }
        return lemmas;
    }

    private static boolean isAuxiliaryPartOfSpeech(LuceneMorphology morphology, String word) {
        List<String> wordProperties = morphology.getMorphInfo(word);
        for (String property : wordProperties) {
            for (String auxiliaryPart : AUXILIARY_PARTS_OF_SPEECH) {
                if (property.contains(auxiliaryPart)) {
                    return true;
                }
            }
        }
        return false;
    }
}