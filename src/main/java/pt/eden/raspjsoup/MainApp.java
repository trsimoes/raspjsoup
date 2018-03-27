package pt.eden.raspjsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * @author : trsimoes
 */
public class MainApp {
    public static void main(String[] args) throws IOException {
        Document doc = Jsoup.connect("http://en.wikipedia.org/").get();
        System.out.println(doc.title());
        Elements newsHeadlines = doc.select("#mp-itn b a");
        for (Element headline : newsHeadlines) {
            System.out.println(headline.attr("title") + "\n" + headline.absUrl("href"));
        }
    }
}
