package pt.eden.raspjsoup;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import us.codecraft.xsoup.Xsoup;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * @author : trsimoes
 */
public class SantanderAppV2 {

    public static void main(String[] args) {
        try {

            String url;
            Map<String, String> cookies;
            Document document;
            Response response;

            //grab login form page first
            url = "https://www.particulares.santandertotta.pt/bepp/sanpt/usuarios/login/?";
            response = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10 * 1000)
                    .followRedirects(true)
                    .execute();
            cookies = response.cookies();
            document = response.parse();
            Element loginForm = document.getElementsByTag("form").get(0);
            Elements inputElements = loginForm.getElementsByTag("input");

            Scanner scan = new Scanner(System.in);
            System.out.print("Username: ");
            String username = scan.nextLine();
            System.out.print("Password: ");
            String password = scan.nextLine();

            Map<String, String> mapParams = new HashMap<>();
            for (Element inputElement : inputElements) {
                String key = inputElement.attr("name");
                String value = inputElement.attr("value");

                if (key.equals("identificacionUsuario"))
                    value = username;
                else if (key.equals("claveConsultiva"))
                    value = password;
                mapParams.put(key, value);
            }

            // hidden OGC_TOKEN
            url = "https://www.particulares.santandertotta.pt/nbp_guard";
            document = Jsoup.connect(url)
                    .header("FETCH-CSRF-TOKEN", "1")
                    .post();
            String token = document.body().html();
            final String[] split = token.split(":");
            mapParams.put(split[0], split[1]);

            //URL found in form's action attribute
            url = "https://www.particulares.santandertotta.pt/bepp/sanpt/usuarios/login/?";
            response = Jsoup.connect(url)
                    .referrer("https://www.particulares.santandertotta.pt/bepp/sanpt/usuarios/login/?")
                    .userAgent("Mozilla/5.0")
                    .timeout(10 * 1000)
                    .data(mapParams)
                    .cookies(cookies)
                    .followRedirects(true).execute();
            cookies = response.cookies();

            url = "https://www.particulares.santandertotta.pt/bepp/sanpt/cuentas/listadomovimientoscuenta/0,,,0.shtml";
            response = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10 * 1000)
                    .followRedirects(true)
                    .cookies(cookies)
                    .execute();

            document = response.parse();

            String elementRaw = Xsoup.compile(
                    "//*[@id=\"innercntnt\"]/div[2]/table/tbody/tr/td[4]/span").evaluate(document)
                    .get();
            System.out.println("Saldo Contabilistico: " + elementRaw);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
