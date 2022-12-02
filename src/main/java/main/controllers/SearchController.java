package main.controllers;

import main.dto.Response;
import main.dto.query.DataObject;
import main.dto.query.QueryObject;
import main.model.Status;
import main.services.IndexManager;
import main.services.SearchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@RestController
public class SearchController {

    private SearchManager searchManager;

    Response emptyQueryReport = new Response(false, "Задан пустой поисковый запрос");
    Response notReadyToSearchReport = new Response(false, "Сайт не готов для поиска");

    @Autowired
    public SearchController(SearchManager searchManager) {
        this.searchManager = searchManager;

    }

    @GetMapping("/search")
    private ResponseEntity queryObject(@RequestParam String query, @RequestParam Optional<String> site,
                                       @RequestParam int offset, @RequestParam int limit) throws InterruptedException, ExecutionException {

        if (query.isEmpty()) {
            return ResponseEntity.ok(emptyQueryReport);
        }

        if (site.isPresent() && !searchManager.checkSiteStatus(site.get()).equals(Status.INDEXED)) {
            return ResponseEntity.ok(notReadyToSearchReport);
        }

        if (!site.isPresent() && IndexManager.getStatusOfIndexing()) {
            return ResponseEntity.ok(notReadyToSearchReport);
        }


        List<DataObject> data = site.isPresent() ? searchManager.searchInSingleSite(query, site.get()) : searchManager.searchByThreads(query);

        return ResponseEntity.ok(new QueryObject(true, data.size(), data.subList(offset, Math.min(offset + limit, data.size()))));
    }
}