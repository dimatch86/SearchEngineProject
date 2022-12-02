package main;

import main.config.Agent;
import main.config.ListOfSites;
import main.config.SiteYaml;
import main.CrudMethods.CrudMethods;
import main.model.Field;
import main.model.Site;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class CmdRunner implements CommandLineRunner {

    private final ListOfSites sitesFromConfiguration;
    private static Agent agent;

    @Autowired
    public CmdRunner(ListOfSites listOfSites, Agent agent) {
        this.sitesFromConfiguration = listOfSites;
        this.agent = agent;
    }

    @Override
    public void run(String... args) {

        CrudMethods.saveField(new Field("title", "title", 1.0));
        CrudMethods.saveField(new Field("body", "body", 0.8));
        List<SiteYaml> sites = sitesFromConfiguration.getSites();
        sites.forEach(site -> {
            CrudMethods.saveSite(new Site(null, new Date(), "", site.getUrl(), site.getName()));
        });
    }

    public static Agent getAgent() {
        return agent;
    }
}