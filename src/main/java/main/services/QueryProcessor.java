package main.services;

import main.CrudMethods.CrudMethods;
import main.dto.query.DataObject;
import main.model.Lemma;
import main.model.Page;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class QueryProcessor {

    private static final double MAX_PERCENT = 0.85;
    private static final int MAX_SNIPPET_LENGTH = 160;
    private List<Lemma> validQueryLemmas;
    private List<DataObject> pageList;
    long start;


    public List<DataObject> search(String query, String site) {

        pageList = new ArrayList<>();
        Set<String> lemmasForSearching = new HashSet<>();
        validQueryLemmas = new ArrayList<>();

        int siteId = CrudMethods.getSiteByUrl(site).getId();

        Set<String> lemmasFromQuery = LemmasGetter.getLemmas(query).keySet();

        List<Lemma> lemmaList = CrudMethods.getLemmasWithOrderWithSiteId(lemmasFromQuery, siteId);


        validQueryLemmas = lemmaList.stream().filter(lemma -> (float)lemma.getFrequency()/CrudMethods.getPagesCountBySite(lemma.getSiteId()) < MAX_PERCENT).collect(Collectors.toList());
        validQueryLemmas.forEach(lemma -> lemmasForSearching.add(lemma.getLemma()));

        start = System.currentTimeMillis();


        List<Page> pageListFromDB = CrudMethods.findPagesNatively(lemmasForSearching, lemmasForSearching.size(), siteId);

        pageListFromDB.forEach(page -> {

            double relevance = 0.0;

            for (Lemma lemma : lemmaList) {

                double rank = CrudMethods.getIndex(page.getId(), lemma.getId()).getRank();
                relevance += rank;
            }

            String body = Jsoup.parse(page.getContent()).body().text();
            String title = Jsoup.parse(page.getContent()).title();

            String snippet = getSnippet(body);
            pageList.add(new DataObject(site, CrudMethods.getSiteByUrl(site).getName(), page.getPath(), title, snippet, relevance));
        });

        System.out.println("DURATION: " + (System.currentTimeMillis() - start));
        return pageList;
    }

    private String getSnippet(String body) {

        Set<String> setOfSentences = new HashSet<>();
        Set<String> setOfContentWords = new HashSet<>();
        List<String> lemmas = new ArrayList<>();

        validQueryLemmas.forEach(lemma -> lemmas.add(lemma.getLemma()));
        String[] sentences = body.split("\\.\\s+");
        for (String sentence : sentences) {
            String[] words = sentence.split("\\s");

            for (String word : words) {

                Set<String> setOfLemmasOfWord = LemmasGetter.getLemmas(word).keySet();
                List<String> listOfLemmasOfWord = new ArrayList<>(setOfLemmasOfWord);

                if (!listOfLemmasOfWord.isEmpty() && lemmas.contains(listOfLemmasOfWord.get(0))) {
                    setOfSentences.add(sentence);
                    setOfContentWords.add(word);
                }
            }
        }
        String snippet;
        for (String sentence : setOfSentences) {
            for (String word : setOfContentWords) {
                sentence = sentence.replace(word, "<b>" + word + "</b>");
            }

            if (sentence.length() <= MAX_SNIPPET_LENGTH) {
                return sentence;
            }
            int start = sentence.indexOf("<b>") < MAX_SNIPPET_LENGTH/2 ? 0 : sentence.indexOf("<b>") - 50;
            int end = sentence.indexOf("<b>") < MAX_SNIPPET_LENGTH/2 ?
                    MAX_SNIPPET_LENGTH : (Math.min(start + MAX_SNIPPET_LENGTH, sentence.length()));

            snippet = sentence.substring(start, end).concat("...");

            return snippet;
        }
        return null;
    }
}