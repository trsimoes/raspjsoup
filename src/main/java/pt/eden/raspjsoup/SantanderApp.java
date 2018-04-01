package pt.eden.raspjsoup;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import us.codecraft.xsoup.Xsoup;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author : trsimoes
 */
public class SantanderApp {

    private List<String> cookies;

    private HttpsURLConnection conn;

    private final String USER_AGENT = "Mozilla/5.0";

    public static void main(String[] args) throws Exception {

        Scanner scan = new Scanner(System.in);
        System.out.print("Username: ");
        String username = scan.nextLine();
        System.out.print("Password: ");
        String password = scan.nextLine();

//        String username = null;
//        String password = null;

        String loginPage = "https://www.particulares.santandertotta.pt/bepp/sanpt/usuarios/login/?";
        String homePage =
                "https://www.particulares.santandertotta.pt/bepp/sanpt/cuentas/listadomovimientoscuenta/0,,,0.shtml?trxId=201803180025635064";

        SantanderApp http = new SantanderApp();

        // make sure cookies is turn on
        CookieHandler.setDefault(new CookieManager());

        // 1. Send a "GET" request, so that you can extract the form's data.
        String page = http.GetPageContent(loginPage);
        System.out.println(page);
        //System.exit(0);
        String postParams = http.getFormParams(page, username, password);
        http.sendPost(loginPage, postParams);

        String result = http.GetPageContent(homePage);
        Document doc = Jsoup.parse(result);
        String elementRaw = Xsoup.compile("/html/body/form[3]/div[7]/div[2]/table/tbody/tr/td[4]/span").evaluate(doc)
                .get();
        System.out.println(elementRaw);

    }

    private void sendPost(String url, String postParams) throws Exception {

        URL obj = new URL(url);
        conn = (HttpsURLConnection) obj.openConnection();

        // Acts like a browser
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Host", "www.particulares.santandertotta.pt:443");
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        for (String cookie : this.cookies) {
            conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
        }
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Referer", "https://www.particulares.santa…pt/bepp/sanpt/usuarios/login/?");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", Integer.toString(postParams.length()));

        conn.setDoOutput(true);
        conn.setDoInput(true);

        // Send post request
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(postParams);
        wr.flush();
        wr.close();

        int responseCode = conn.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + postParams);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        System.out.println(response.toString());

    }

    private String GetPageContent(String url) throws Exception {

        URL obj = new URL(url);
        conn = (HttpsURLConnection) obj.openConnection();

        // default is GET
        conn.setRequestMethod("GET");

        conn.setUseCaches(false);

        // act like a browser
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/");
//        conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        conn.setRequestProperty("Accept-Language", "pt-PT,pt;q=0.8,en;q=0.5,en-US;q=0.3");
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Host", "www.particulares.santandertotta.pt");
        conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        if (cookies != null) {
            for (String cookie : this.cookies) {
                conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
            }
        }
        int responseCode = conn.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // Get the response cookies
        setCookies(conn.getHeaderFields().get("Set-Cookie"));

        return response.toString();

    }

    private void setCookies(List<String> cookies) {
        this.cookies = cookies;
    }

    private String getFormParams(String html, String username, String password) throws IOException {

        System.out.println("Extracting form's data...");

        Document doc = Jsoup.parse(html);

        // Google form id
        Element loginform = doc.getElementsByTag("form").get(0);
        Elements inputElements = loginform.getElementsByTag("input");
        List<String> paramList = new ArrayList<String>();
        for (Element inputElement : inputElements) {
            String key = inputElement.attr("name");
            String value = inputElement.attr("value");

            if (key.equals("identificacionUsuario"))
                value = username;
            else if (key.equals("claveConsultiva"))
                value = password;
            //paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
            paramList.add(key + "=" + value);
        }

        final Connection connect = Jsoup.connect("https://www.particulares.santandertotta.pt/nbp_guard");
        connect.header("FETCH-CSRF-TOKEN", "1");
        final Document post = connect.post();
        String token = post.body().html();
        final String[] split = token.split(":");
        paramList.add(split[0] + "=" + split[1]);

        // build parameters list
        StringBuilder result = new StringBuilder();
        for (String param : paramList) {
            if (result.length() == 0) {
                result.append(param);
            } else {
                result.append("&").append(param);
            }
        }
        return result.toString();
    }
}
