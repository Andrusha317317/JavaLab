import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

/**
 * Варіант 20 (C4 = 0).
 * Доменна область: Предмети одягу.
 */
public class Main {

    /**
     * Точка входу в програму.
     *
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {
        // Параметри завдання
        int targetSize = 500;
        int skipN = 5;
        String skipCity = "Kyiv";
        int minMonths = 0;
        int maxMonths = 60;

        System.out.println("--- 1. Генерація та Gatherer ---");
        // 1. Генерація та Gatherer (Вимога п.4-5)
        List<ClothingItem> initialList = Stream.generate(Main::generateRandomItem)
                .gather(new SkipByCityGatherer(skipN, skipCity))
                .limit(targetSize)
                .toList();

        System.out.println("Згенеровано елементів: " + initialList.size());

        System.out.println("\n--- 2. Фільтрація та Групування ---");
        // 2. Фільтрація та Групування (Вимога п.6)
        Map<String, List<ClothingItem>> groupedByFabric = initialList.stream()
                .filter(item -> {
                    long months = item.getMonthsSinceProduction();
                    return months >= minMonths && months <= maxMonths;
                })
                .collect(Collectors.groupingBy(ClothingItem::fabricType));

        // Вивід результатів групування (Вимога показати результат п.6)
        groupedByFabric.forEach((fabric, items) ->
                System.out.printf("Тканина: %-10s | Кількість: %d%n", fabric, items.size())
        );

        // Підготовка списку для статистики
        List<ClothingItem> filteredList = groupedByFabric.values().stream()
                .flatMap(List::stream)
                .toList();

        System.out.println("\n--- 3. Статистичні дані (Власний Collector) ---");
        // 3. Статистика (Вимога п.7)
        PriceStatistics stats = filteredList.stream()
                .map(ClothingItem::price)
                .collect(new PriceStatsCollector());

        System.out.printf("Мінімум:   %.2f грн%n", stats.getMin());
        System.out.printf("Максимум:  %.2f грн%n", stats.getMax());
        System.out.printf("Середнє:   %.2f грн%n", stats.getAverage());
        System.out.printf("Відхилення: %.2f%n", stats.getStandardDeviation());

        System.out.println("\n--- 4. Аналіз викидів (IQR) ---");
        // 4. Аналіз викидів (Вимога п.8-10)
        Map<String, Long> outliersResult = analyzeOutliers(filteredList);
        System.out.println("Результат: " + outliersResult);
    }

    /**
     * Генерує випадковий об'єкт предмету одягу.
     *
     * @return новий екземпляр ClothingItem з випадковими даними.
     */
    private static ClothingItem generateRandomItem() {
        String[] cities = {"Kyiv", "Lviv", "Odesa", "Kharkiv", "Dnipro"};
        String[] fabrics = {"Cotton", "Wool", "Silk", "Polyester", "Linen"};
        String[] names = {"T-Shirt", "Jeans", "Jacket", "Dress", "Scarf"};

        var rnd = ThreadLocalRandom.current();

        String city = cities[rnd.nextInt(cities.length)];
        String fabric = fabrics[rnd.nextInt(fabrics.length)];
        String name = names[rnd.nextInt(names.length)];
        // Ціна від 100 до 5000 грн
        double price = 100 + rnd.nextDouble() * 4900;
        // Дата від сьогодні до 10 років назад
        LocalDate date = LocalDate.now().minusMonths(rnd.nextInt(120));

        return new ClothingItem(name, fabric, city, date, price);
    }

    /**
     * Аналізує список на наявність викидів за методом IQR (міжквартильного розмаху).
     *
     * @param items список предметів одягу
     *              [cite_start]@return мапа з кількістю нормальних значень ("data") та викидів ("outliers") [cite: 16]
     */
    private static Map<String, Long> analyzeOutliers(List<ClothingItem> items) {
        List<Double> prices = items.stream()
                .map(ClothingItem::price)
                .sorted()
                .toList();

        if (prices.isEmpty()) return Map.of("data", 0L, "outliers", 0L);

        // Розрахунок квартилів
        double q1 = getPercentile(prices, 25);
        double q3 = getPercentile(prices, 75);
        double iqr = q3 - q1;

        // Визначення меж викидів
        double lowerBound = q1 - 1.5 * iqr;
        double upperBound = q3 + 1.5 * iqr;

        // Групування (partitioning)
        Map<Boolean, Long> grouped = items.stream()
                .collect(Collectors.partitioningBy(item ->
                                item.price() < lowerBound || item.price() > upperBound,
                        Collectors.counting()
                ));

        // Формування структури згідно з вимогами
        Map<String, Long> result = new HashMap<>();
        result.put("data", grouped.getOrDefault(false, 0L));
        result.put("outliers", grouped.getOrDefault(true, 0L));

        return result;
    }

    /**
     * Допоміжний метод для розрахунку процентиля.
     */
    private static double getPercentile(List<Double> sortedData, double percentile) {
        int index = (int) Math.ceil(percentile / 100.0 * sortedData.size());
        return sortedData.get(Math.max(0, index - 1));
    }
}

/**
 * Клас-модель предмету одягу (Record).
 * Поля відповідають варіанту 0.
 *
 * @param name           Назва
 * @param fabricType     Тип тканини (Поле В)
 * @param productionCity Місто виробництва (Поле A)
 * @param productionDate Дата пошиття
 * @param price          Ціна в грн (Поле Г)
 */
record ClothingItem(
        String name,
        String fabricType,
        String productionCity,
        LocalDate productionDate,
        double price
) {
    /**
     * Розраховує кількість місяців з моменту виробництва (Параметр Б).
     *
     * @return кількість повних місяців
     */
    public long getMonthsSinceProduction() {
        return ChronoUnit.MONTHS.between(productionDate, LocalDate.now());
    }
}

/**
 * Власний Gatherer для пропуску перших N елементів, що відповідають певному критерію.
 * Реалізує вимогу п.4.
 */
class SkipByCityGatherer implements Gatherer<ClothingItem, AtomicInteger, ClothingItem> {
    private final int maxSkip;
    private final String targetCity;

    /**
     * Конструктор Gatherer.
     *
     * @param maxSkip    кількість елементів для пропуску (N)
     * @param targetCity значення Поля А, за яким пропускати
     */
    public SkipByCityGatherer(int maxSkip, String targetCity) {
        this.maxSkip = maxSkip;
        this.targetCity = targetCity;
    }

    @Override
    public Supplier<AtomicInteger> initializer() {
        return () -> new AtomicInteger(0);
    }

    @Override
    public Integrator<AtomicInteger, ClothingItem, ClothingItem> integrator() {
        return (counter, item, downstream) -> {
            // Перевірка умови пропуску: лічильник < N та місто співпадає
            if (counter.get() < maxSkip && item.productionCity().equals(targetCity)) {
                counter.incrementAndGet();
                return true; // Пропускаємо елемент (не викликаємо downstream.push)
            }
            return downstream.push(item); // Передаємо елемент далі
        };
    }
}

/**
 * Клас для накопичення статистичних даних (mutable container).
 * Використовується в Custom Collector.
 */
class PriceStatistics {
    private double min = Double.MAX_VALUE;
    private double max = Double.MIN_VALUE;
    private double sum = 0;
    private double sumSq = 0;
    private long count = 0;

    /**
     * Додає значення ціни до статистики.
     */
    public void accept(double price) {
        if (price < min) min = price;
        if (price > max) max = price;
        sum += price;
        sumSq += price * price;
        count++;
    }

    /**
     * Об'єднує дві статистики (для паралельних стрімів).
     */
    public PriceStatistics combine(PriceStatistics other) {
        if (other.min < min) min = other.min;
        if (other.max > max) max = other.max;
        sum += other.sum;
        sumSq += other.sumSq;
        count += other.count;
        return this;
    }

    public double getMin() {
        return count == 0 ? 0 : min;
    }

    public double getMax() {
        return count == 0 ? 0 : max;
    }

    public double getAverage() {
        return count == 0 ? 0 : sum / count;
    }

    /**
     * Розрахунок стандартного відхилення.
     */
    public double getStandardDeviation() {
        if (count <= 1) return 0;
        double variance = (sumSq - (sum * sum) / count) / (count - 1);
        return Math.sqrt(Math.max(0, variance));
    }
}

/**
 * Власний Collector для збору статистики (п.7).
 */
class PriceStatsCollector implements Collector<Double, PriceStatistics, PriceStatistics> {
    @Override
    public Supplier<PriceStatistics> supplier() {
        return PriceStatistics::new;
    }

    @Override
    public BiConsumer<PriceStatistics, Double> accumulator() {
        return PriceStatistics::accept;
    }

    @Override
    public BinaryOperator<PriceStatistics> combiner() {
        return PriceStatistics::combine;
    }

    @Override
    public Function<PriceStatistics, PriceStatistics> finisher() {
        return Function.identity();
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.IDENTITY_FINISH);
    }
}