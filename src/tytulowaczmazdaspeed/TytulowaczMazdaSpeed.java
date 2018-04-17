/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tytulowaczmazdaspeed;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author Bartek
 */
public class TytulowaczMazdaSpeed {

    /**
     * @param args the command line arguments
     */
    public static String main(String url) throws IOException {
        // TODO code application logic here
        GetURL get = new GetURL();
        String site = get.getUrlSource(url);
        //System.out.println(site);
        String jSonData = site.substring(site.indexOf("GPT.targeting = ") + 16, site.indexOf("googletag.cmd.push") - 1);
        //System.out.println(jSonData);
        JSONObject obj = new JSONObject(jSonData);
        String make = obj.getJSONArray("make").getString(0);
        make = capitalize(make);
        String model = obj.getJSONArray("model").getString(0);
        model = capitalize(model);
        model = checkModel(model);
        float engine = obj.getJSONArray("engine_capacity").getInt(0);
        String engineDecimal = convertEngineSize(engine);
        String fuel = obj.getJSONArray("fuel_type").getString(0);
        fuel = fuelTypeToShort(fuel);
        String year = obj.getJSONArray("year").getString(0);
        String otomotoID = obj.getString("ad_id");
        String price = obj.getString("price_raw");
        price = formatPrice(price);
        String businessType = obj.getString("private_business");
        //System.out.println(businessType);
        String location = getLocation(site, businessType);
        return make + model + " " + engineDecimal + " " + fuel + ", " + year + ", (otomoto: " + otomotoID + "), " + price + " zł, " + location + ".";

    }

    static String convertEngineSize(float n) {

        DecimalFormat f = new DecimalFormat("##.0");
        n = n / 1000;
        String engineF = f.format(n);
        engineF = engineF.replace(",", ".");

        return engineF;
    }

    static String capitalize(final String line) {
        if (line.equals("mx-3") || line.equals("mx-5") || line.equals("mx-6") || line.equals("rx-7")
                || line.equals("rx-8") || line.equals("mpv") || line.equals("cx-3") || line.equals("cx-5") || line.equals("cx-7")
                || line.equals("cx-9")) {
            return line.toUpperCase();
        } else {
            return Character.toUpperCase(line.charAt(0)) + line.substring(1);
        }
    }

    static String fuelTypeToShort(String fuel) {
        switch (fuel) {
            case "petrol":
                fuel = "Pb";
                break;
            case "petrol-lpg":
                fuel = "Pb/LPG";
                break;
            case "diesel":
                fuel = "ON";
                break;
            case "hybrid":
                fuel = "Hybryda";
                break;
        }
        return fuel;
    }

    //temporary permanent fix
    static String convertToPolChar(String location) {
        location = location.replace("\\u0104", "Ą");
        location = location.replace("\\u0106", "Ć");
        location = location.replace("\\u0118", "Ę");
        location = location.replace("\\u0141", "Ł");
        location = location.replace("\\u0143", "Ń");
        location = location.replace("\\u00d3", "Ó");
        location = location.replace("\\u015a", "Ś");
        location = location.replace("\\u0179", "Ź");
        location = location.replace("\\u017b", "Ż");
        location = location.replace("\\u0105", "ą");
        location = location.replace("\\u0107", "ć");
        location = location.replace("\\u0119", "ę");
        location = location.replace("\\u0142", "ł");
        location = location.replace("\\u0144", "ń");
        location = location.replace("\\u00f3", "ó");
        location = location.replace("\\u015b", "ś");
        location = location.replace("\\u017a", "ź");
        location = location.replace("\\u017c", "ż");
        return location;
    }

    static String formatPrice(String price) {

        //String number = "1000500000.574";
        double amount = Double.parseDouble(price);
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMANY);
        DecimalFormat df = (DecimalFormat) nf;
        //DecimalFormat formatter = new DecimalFormat("#,###");
        String priceFormatted = df.format(amount);
        return priceFormatted;
    }

    static String checkModel(String model) {
        if (model.length() > 1) {
            model = " " + model;
        }
        return model;
    }

    static String getLocation(String site, String businessType) {
        String location = "";
        if ("private".equals(businessType)) {
            Document doc = Jsoup.parse(site);//.getElementsMatchingText("^\\d{2}-\\d{3}"); //\\s\\w+.\\S+$
            location = doc.select("span.seller-box__seller-address__label").get(0).text();
            //System.out.println(location);
            Pattern p = Pattern.compile("(^[^\\(]+)");   // the pattern to search for
            Matcher m = p.matcher(location);

            // if we find a match, get the group 
            if (m.find()) {
                // we're only looking for one group, so get it
                location = m.group(0);
                location = location.trim();
            }

        } else {
            Document doc = Jsoup.parse(site);//.getElementsMatchingText("^\\d{2}-\\d{3}"); //\\s\\w+.\\S+$
            location = doc.select("span.seller-box__seller-address__label").get(0).text();
            //System.out.println(location);

            //String stringToSearch = "Four score and seven years ago our fathers ...";
            Pattern p = Pattern.compile("(?<=\\d{2}-\\d{3}.).+?(?=,)");   // the pattern to search for
            Matcher m = p.matcher(location);

            // if we find a match, get the group 
            if (m.find()) {
                // we're only looking for one group, so get it
                location = m.group(0);
            }

        }
        return location;
    }

}
