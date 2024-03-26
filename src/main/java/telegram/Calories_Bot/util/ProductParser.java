package telegram.Calories_Bot.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import telegram.Calories_Bot.entity.Product;

import java.io.IOException;
import java.net.ConnectException;
import java.util.LinkedList;


public class ProductParser {

    /**
     * @return Список всех продуктов, взятых с сайта. Там их будет +-11 тысяч.
     */
    public static LinkedList<Product> getAllProductsFromCalorizatorWebSite() {

        LinkedList<Product> products = new LinkedList<>();

        try {
            for (int pageNumber = 0; pageNumber < 83; pageNumber++) {

                Document doc = Jsoup.connect("https://calorizator.ru/product/all?page=" + pageNumber).get();

                //  Парсим аттрибуты со значением 'even' и 'odd' (Они чередуются друг за другом в HTML файле)
                Elements elements = doc.getElementsByAttributeValueMatching("class", "^(even.*|odd.*)");

                for (Element element : elements) {

                    Product product = new Product();

                    product.setName(element.getElementsByAttribute("href").text());

                    // В HTML-документе встречается пустота вместо значения, поэтому делаем проверку на пустую строку
                    String protein = element.getElementsByAttributeValueEnding("class","protein-value").text();
                    product.setProtein((protein.isEmpty()) ? 0.0f : Float.parseFloat(protein));

                    String fat = element.getElementsByAttributeValueEnding("class","fat-value").text();
                    product.setFat((fat.isEmpty()) ? 0.0f : Float.parseFloat(fat));

                    String carbohydrate = element.getElementsByAttributeValueEnding("class","carbohydrate-value").text();
                    product.setCarbohydrate((carbohydrate.isEmpty()) ? 0.0f : Float.parseFloat(carbohydrate));

                    String kcal = element.getElementsByAttributeValueEnding("class","kcal-value").text();
                    product.setKcal((kcal.isEmpty()) ? 0 : Integer.parseInt(kcal));

                    products.add(product);
                }

                System.out.println("Page " + pageNumber + " was added to List");
            }
        } catch (ConnectException e) {
            System.err.println("Не получилось соединиться с сайтом");
        } catch (IOException e) {
            System.err.println("Ошибка ввода-вывода");
            System.out.println(e.getMessage());
        }
        return products;
    }
}
