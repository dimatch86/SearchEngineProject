package main.services;

import lombok.NoArgsConstructor;
import main.CmdRunner;
import main.CrudMethods.CrudMethods;
import main.model.Lemma;
import main.model.Page;
import main.model.Status;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RecursiveTask;

@Service
@NoArgsConstructor
public class NetParser extends RecursiveTask<Integer> {

    private String url;
    private int siteId;
    private static CopyOnWriteArraySet<String> visitedSites = new CopyOnWriteArraySet<>();
    private String rootLink;
    private final String userAgent = CmdRunner.getAgent().getUserAgent();
    private final String referrer = CmdRunner.getAgent().getReferrer();


    public NetParser(String url, String rootLink, int siteId) {
        this.url = url;
        this.rootLink = rootLink;
        this.siteId = siteId;
        visitedSites.add(url);
    }

    @Override
    protected Integer compute() {
        List<NetParser> subtasks = new ArrayList<>();
        int code = 0;
        Document document = null;

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            Connection connection = Jsoup.connect(url).userAgent(userAgent).referrer(referrer);
            document = connection.get();
            code = connection.response().statusCode();

        }  catch (HttpStatusException hsEx) {
            System.out.println((hsEx.getUrl() + " === " + hsEx.getStatusCode()));

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (document != null) {

            String linkForDB = url.replace(rootLink, "/");
            System.out.println(url + "  " + siteId);
            String content = document.html();
            Page page = CrudMethods.savePage(new Page(linkForDB, code, content, siteId));
            CrudMethods.updateStatusTime(siteId);

            String title = document.title();
            String body = document.body().text();
            String textInPage = body.concat(" ").concat(title);
            try {
                List<Lemma> list = CrudMethods.saveAllLemma(textInPage, siteId);
                CrudMethods.saveAllIndexes(list, title, body, page.getId());

            } catch (IOException e) {
                e.printStackTrace();
            }

            Elements elements = document.select("a[href]");
            for (Element e : elements) {
                String childLink = e.attr("abs:href");

                if (IndexManager.getStopMarker()) {
                    CrudMethods.updateStatus(siteId, Status.FAILED);
                    CrudMethods.updateLastError(siteId, "Индексация прервана пользователем");
                    break;
                }

                if (IndexManager.getStopMarkerForSinglePage()) {
                    break;
                }

                if (visitedSites.contains(childLink)) {
                    continue;
                }

                if (isValid(childLink)) {
                    NetParser task = new NetParser(childLink, rootLink, siteId);
                    task.fork();
                    subtasks.add(task);
                }
            }
            for (NetParser task : subtasks) {
                task.join();
            }
        }
        return siteId;

    }
    private boolean isValid(String url) {
        return url.startsWith(rootLink)
                && !url.contains("?month")
                && !url.contains("login?")
                && !url.contains("#")
                && !url.contains("_info")
                && !FilenameUtils.getExtension(url).matches("(pptx|jpg|png|gif|bmp|pdf|xml|doc|ppt|docx|JPG|eps|PDF|jpeg|xlsx|webp|xls|nc|zip|m|fig)");
    }

    public static Set<String> getVisited() {
        return visitedSites;
    }
}