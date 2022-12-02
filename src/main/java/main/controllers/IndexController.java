package main.controllers;

import main.dto.Response;
import main.dto.statistics.StatisticsObject;
import main.services.IndexManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {


    private IndexManager indexManager;

    private final Response trueResponse = new Response(true);
    private Response falseResponse;

    @Autowired
    public IndexController(IndexManager indexManager) {
        this.indexManager = indexManager;
    }

    @GetMapping("/statistics")
    private StatisticsObject statisticsObject() {

        return indexManager.getStatistics();
    }


    @GetMapping("/startIndexing")
    private Response start() throws InterruptedException {

        falseResponse = new Response(false, "Индексация уже запущена");

        return indexManager.startIndexingByThreads() ? trueResponse : falseResponse;
    }


    @GetMapping("/stopIndexing")
    private Response stop() {

        falseResponse = new Response(false, "Индексация не запущена");

        return indexManager.stopIndexing() ? trueResponse : falseResponse;
    }


    @PostMapping("/indexPage")
    private Response indexPage(@RequestParam String url) {

        falseResponse = new Response(false, "Данная страница находится за пределами " +
                "сайтов, указанных в конфигурационном файле, или уже существует в базе");

        return indexManager.addSinglePage(url) ? trueResponse : falseResponse;
    }
}