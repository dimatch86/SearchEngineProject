package main.services;

import main.CrudMethods.CrudMethods;
import main.dto.query.DataObject;
import main.model.Site;
import main.model.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service
public class SearchManager {

    private QueryProcessor queryProcessor;

    private final int coresCount = Runtime.getRuntime().availableProcessors();
    private List<DataObject> dataObjectList;
    private List<Site> sitesToSearch = CrudMethods.getSites();
    private List<Future<List<DataObject>>>  futureList = new ArrayList<>();
    private static Map<String, List<DataObject>> cacheForAllSites= new HashMap<>();
    private static Map<String, List<DataObject>> cacheForSingleSite = new HashMap<>();
    private Comparator<DataObject> compareByRelevance = Comparator.comparing(DataObject::getRelevance).reversed();



    @Autowired
    public SearchManager(QueryProcessor queryProcessor) {
        this.queryProcessor = queryProcessor;
    }

    public List<DataObject> searchByThreads(String query) throws InterruptedException, ExecutionException {

        dataObjectList = new ArrayList<>();

        if (cacheForAllSites.containsKey(query)) {
            return cacheForAllSites.get(query);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(coresCount);
        futureList = new ArrayList<>();

        sitesToSearch.forEach(site -> {

            futureList.add(executorService.submit(() -> queryProcessor.search(query, site.getUrl())));
        });

        while (!isDone(futureList)) {
            Thread.sleep(300);
        }

        for (Future<List<DataObject>> future : futureList) {
            dataObjectList.addAll(future.get());
            break;
        }
        executorService.shutdown();

        dataObjectList.sort(compareByRelevance);
        cacheForAllSites.put(query, dataObjectList);
        return dataObjectList;
    }

    public List<DataObject> searchInSingleSite(String query, String site) {
        dataObjectList = new ArrayList<>();

        if (cacheForSingleSite.containsKey(query)) {
            return cacheForSingleSite.get(query);
        }
        dataObjectList = queryProcessor.search(query, site);
        cacheForSingleSite.put(query, dataObjectList);
        return dataObjectList;
    }

    public Status checkSiteStatus(String site) {
        int siteId = CrudMethods.getSiteByUrl(site).getId();
        return CrudMethods.getSite(siteId).getStatus();
    }

    private boolean isDone(List<Future<List<DataObject>>> list) {
        for (Future<List<DataObject>> future : list) {
            if (!future.isDone()) {
                return false;
            }
        }
        return true;
    }
}