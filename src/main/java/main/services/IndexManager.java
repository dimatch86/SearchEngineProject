package main.services;

import main.CrudMethods.CrudMethods;
import main.dto.statistics.Detailed;
import main.dto.statistics.Statistics;
import main.dto.statistics.StatisticsObject;
import main.dto.statistics.Total;
import main.model.Site;
import main.model.Status;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class IndexManager {

    private static volatile boolean isStopped;
    private static boolean isStoppedForSinglePage;

    private ForkJoinPool pool = new ForkJoinPool();
    private ExecutorService executorService;

    private Future<Integer> future;
    private List<Future<Integer>> futureList = new ArrayList<>();
    private static Total total = new Total();


    public boolean startIndexingByThreads() {

        if (isReadyToIndexing()) {

            NetParser.getVisited().clear();
            CrudMethods.cleanDB();
            getFutures().clear();
            total.setIndexing(true);
            isStopped = false;
            isStoppedForSinglePage = false;
            executorService = Executors.newCachedThreadPool();

            List<Site> sitesToIndex = CrudMethods.getSites();
            sitesToIndex.forEach(site -> {

                CrudMethods.updateStatus(site.getId(), Status.INDEXING);
                CrudMethods.updateLastError(site.getId(), "");
                String mainPage = site.getUrl().concat("/");
                future = executorService.submit(() -> pool.invoke(new NetParser(mainPage, mainPage, site.getId())));
                futureList.add(future);
            });

            executorService.shutdown();
            return true;
        }
        return false;
    }

    public boolean stopIndexing() {

        isStopped = true;
        total.setIndexing(false);
        if (!isReadyToIndexing()) {

            getFutures().forEach(future -> {
                if (!future.isDone()) {
                    future.cancel(true);
                }
            });
            return true;
        }
        return false;
    }

    public boolean addSinglePage(String url) {
        isStoppedForSinglePage = true;

        for(Site site : CrudMethods.getSites()) {

            String path = url.replace(site.getUrl(), "");
            if (url.contains(site.getUrl()) && CrudMethods.getPageByPath(path) == null) {

                pool.invoke(new NetParser(url, site.getUrl().concat("/"), site.getId()));
                return true;
            }
        }
        return false;
    }

    private boolean isReadyToIndexing() {

        return getFutures().isEmpty() || isDone();
    }

    private boolean isDone() {
        for (Future<Integer> future : getFutures()) {
            if (!future.isDone()) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEngineIndexing() {
        for (Site site : CrudMethods.getSites()) {
            if (site.getStatus() != null && site.getStatus().equals(Status.INDEXING)) {
                return true;
            }
        }
        return false;
    }

    public void changeStatusToIndexed() throws ExecutionException, InterruptedException {
        for (Future<Integer> future : getFutures()) {
            if (future.isDone()) {
                int siteId = future.get();
                CrudMethods.updateStatus(siteId, Status.INDEXED);
            }
        }
    }


    public static boolean getStopMarker() {
        return isStopped;
    }
    public static boolean getStopMarkerForSinglePage() {
        return isStoppedForSinglePage;
    }

    public static boolean getStatusOfIndexing() {
        return total.isIndexing();
    }

    public List<Future<Integer>> getFutures() {
        return futureList;
    }

    public StatisticsObject getStatistics() {


        List<Detailed> detailedList = new ArrayList<>();
        StatisticsObject statisticsObject = new StatisticsObject();


        CrudMethods.getSites().forEach(site -> {

            try {
                changeStatusToIndexed();
            } catch (CancellationException | ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            detailedList.add(new Detailed(site.getUrl(), site.getName(), site.getStatus(),
                    site.getStatusTime(), site.getLastError(), CrudMethods.getPagesCountBySite(site.getId()), CrudMethods.getLemmasCountBySite(site.getId())));
        });
        if (!isEngineIndexing()) {
            total.setIndexing(false);
        }

        statisticsObject.setResult(true);
        statisticsObject.setStatistics(new Statistics((new Total(CrudMethods.getSitesCount(),
                CrudMethods.getPagesCount(), CrudMethods.getLemmasCount(), total.isIndexing())),
                detailedList));
        return statisticsObject;
    }
}