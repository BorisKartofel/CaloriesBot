package telegram.Calories_Bot.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import telegram.Calories_Bot.entity.Product;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class ProductParser {

    /**
     * @return Список всех продуктов, взятых с сайта. Там их будет +-11 тысяч.
     */

    public static LinkedList<Product> getAllProductsFromCalorizatorWebSite() {

        final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        LinkedList<Product> products = new LinkedList<>();
        final Lock lock = new ReentrantLock();
        final Semaphore semaphore = new Semaphore(40);

        try {

            for (int pageNumber = 0; pageNumber < 83; pageNumber++) {

                int pageNumberInLocalThread = pageNumber;
                executor.submit(() -> {

                    try {

                        Document doc;
                        try {
                            semaphore.acquire();
                            doc = Jsoup.connect(
                                    "https://calorizator.ru/product/all?page=" + pageNumberInLocalThread).get();
                            semaphore.release();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }


                        //  Парсим аттрибуты со значением 'even' и 'odd' (Они чередуются друг за другом в HTML файле)
                        Elements elements = doc.getElementsByAttributeValueMatching("class", "^(even.*|odd.*)");

                        for (Element element : elements) {

                            Product product = new Product();

                            product.setName(element.getElementsByAttribute("href").text());

                            // В HTML-документе встречается пустая строка вместо значения, поэтому делаем проверку на null
                            String protein = element.getElementsByAttributeValueEnding("class", "protein-value").text();
                            product.setProtein((protein.isEmpty()) ? 0.0f : Float.parseFloat(protein));

                            String fat = element.getElementsByAttributeValueEnding("class", "fat-value").text();
                            product.setFat((fat.isEmpty()) ? 0.0f : Float.parseFloat(fat));

                            String carbohydrate = element.getElementsByAttributeValueEnding("class", "carbohydrate-value").text();
                            product.setCarbohydrate((carbohydrate.isEmpty()) ? 0.0f : Float.parseFloat(carbohydrate));

                            String kcal = element.getElementsByAttributeValueEnding("class", "kcal-value").text();
                            product.setKcal((kcal.isEmpty()) ? 0 : Integer.parseInt(kcal));

                            lock.lock();
                            products.add(product);
                            lock.unlock();
                        }

                        System.out.println("Page " + pageNumberInLocalThread + " was added to List");

                    } catch (IOException e) {
                        System.err.println("Ошибка при попытке соединиться со страницей №" + pageNumberInLocalThread);
                        System.err.println(e.getMessage());
                    }
                });
            }

            executor.shutdown();
            if (!executor.awaitTermination(20, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }

        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        return products;
    }
}
