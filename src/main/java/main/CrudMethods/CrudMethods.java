package main.CrudMethods;

import main.model.*;
import main.repositories.*;
import main.services.LemmasGetter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class CrudMethods {

    private static LemmaRepository lemmaRepository;
    private static PageRepository pageRepository;
    private static SiteRepository siteRepository;
    private static FieldRepository fieldRepository;
    private static IndexRepository indexRepository;

    @Autowired
    public CrudMethods(LemmaRepository lemmaRepository, PageRepository pageRepository, SiteRepository siteRepository,
                       FieldRepository fieldRepository, IndexRepository indexRepository) {
        CrudMethods.lemmaRepository = lemmaRepository;
        CrudMethods.pageRepository = pageRepository;
        CrudMethods.siteRepository = siteRepository;
        CrudMethods.fieldRepository = fieldRepository;
        CrudMethods.indexRepository = indexRepository;

    }

    public static double getRank(Map<String, Integer> mapTitle, Map<String, Integer> mapBody, String lemma) {
        double weight;

        double rankTitle = 0;
        double rankBody = 0;
        if (mapTitle.containsKey(lemma)) {
            weight = fieldRepository.findFieldBySelector("title").getWeight();
            rankTitle = mapTitle.get(lemma) * weight;

        }
        if (mapBody.containsKey(lemma)) {
            weight = fieldRepository.findFieldBySelector("body").getWeight();
            rankBody = mapBody.get(lemma) * weight;
        }
        return rankTitle + rankBody;
    }

    public static synchronized List<Lemma> saveAllLemma(String textInPage, int siteId) throws IOException {
        List<Lemma> lemmasEntityList = new ArrayList<>();
        LemmasGetter.getLemmas(textInPage).keySet().forEach(lemma -> lemmasEntityList.add(new Lemma(lemma, siteId)));
        return lemmaRepository.saveAll(lemmasEntityList);
    }

    public static synchronized void saveAllIndexes(List<Lemma> list, String title, String body, long pageId) throws IOException {
        Map<String, Integer> lemmasInTitle = LemmasGetter.getLemmas(title);
        Map<String, Integer> lemmasInBody = LemmasGetter.getLemmas(body);
        List<Index> indexEntityList = new ArrayList<>();
        list.forEach(lemma -> {
            long lemmaId = lemma.getId();
            double rank = getRank(lemmasInTitle, lemmasInBody, lemma.getLemma());
            indexEntityList.add(new Index(pageId, lemmaId, rank));
        });
        indexRepository.saveAll(indexEntityList);


    }
    public static Page savePage(Page page) {
        return pageRepository.save(page);
    }
    public static void saveSite(Site site) {
        siteRepository.save(site);
    }
    public static void saveField(Field field) {
        fieldRepository.save(field);
    }


    public static long getPagesCount() {
        return pageRepository.count();
    }
    public static long getPagesCountBySite(int siteId) {
        return pageRepository.countBySiteId(siteId);
    }

    public static long getLemmasCount() {
        return lemmaRepository.count();
    }
    public static long getLemmasCountBySite(int siteId) {
        return lemmaRepository.countBySiteId(siteId);
    }

    public static List<Site> getSites() {
        return siteRepository.findAll();
    }
    public static long getSitesCount() {
        return siteRepository.count();
    }

    public static void cleanDB() {
       pageRepository.deleteAllInBatch();
       lemmaRepository.deleteAllInBatch();
    }


    public static Site getSite(int id) {
        return siteRepository.findById(id).get();
    }
    public static Site getSiteByUrl(String url) {
        return siteRepository.findSiteByUrl(url);
    }


    public static void updateStatus(int id, Status status) {
        Site site = getSite(id);
        site.setStatus(status);
        siteRepository.save(site);
    }
    public static void updateStatusTime(int id) {
        Site site = getSite(id);
        site.setStatusTime(new Date());
        siteRepository.save(site);
    }

    public static void updateLastError(int id, String error) {
        Site site = siteRepository.findById(id).get();
        site.setLastError(error);
        siteRepository.save(site);
    }


    public static Page getPageByPath(String path) {
        return pageRepository.findByPath(path);
    }

    public static Index getIndex(long pageId, long lemmaId) {
        return indexRepository.findByPageIdAndLemmaId(pageId, lemmaId);
    }

    public static List<Lemma> getLemmasWithOrderWithSiteId(Set<String> list, int siteId) {
        return lemmaRepository.findAllByLemmaInAndSiteIdOrderByFrequencyAsc(list, siteId);
    }


    public static List<Page> findPagesNatively(Set<String> lemmaList, int count, int siteId) {
        return pageRepository.findPagesByNativeQueryWithSiteId(lemmaList, count, siteId);
    }
}