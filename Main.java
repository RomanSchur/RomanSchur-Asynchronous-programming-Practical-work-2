import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

class SquareCalculator implements Callable<CopyOnWriteArraySet<Double>> {
    private final List<Double> numbers;

    public SquareCalculator(List<Double> numbers) {
        this.numbers = numbers;
    }
    @Override
    public CopyOnWriteArraySet<Double> call() throws Exception {
        CopyOnWriteArraySet<Double> results = new CopyOnWriteArraySet<>();

        for (double num : numbers) {
            double square = num * num;
            double roundedSquare = Math.round(square * 100.0) / 100.0;
            results.add(roundedSquare);
            Thread.sleep(10);
        }
        return results;
    }
}
public class Main {

    public static void main(String[] args) {
        Random random = new Random();
        int arraySize = random.nextInt(21) + 40;

        Scanner scanner = new Scanner(System.in);

        System.out.print("Введіть МІНІМАЛЬНЕ значення діапазону: ");
        double MIN_RANGE = scanner.nextDouble();

        System.out.print("Введіть МАКСИМАЛЬНЕ значення діапазону: ");
        double MAX_RANGE = scanner.nextDouble();

        if (MIN_RANGE > MAX_RANGE) {
            System.out.println("Мінімальне значення більше за максимальне. Зміна місцями.");
            double temp = MIN_RANGE;
            MIN_RANGE = MAX_RANGE;
            MAX_RANGE = temp;
        }
        System.out.println("Діапазон генерації: [" + MIN_RANGE + " ... " + MAX_RANGE + "]");

        int NUM_THREADS = 4;
        List<Double> originalArray = new ArrayList<>();

        for (int i = 0; i < arraySize; i++) {
            double randomValue = MIN_RANGE + (MAX_RANGE - MIN_RANGE) * random.nextDouble();
            double roundedValue = Math.round(randomValue * 100.0) / 100.0;
            originalArray.add(roundedValue);
        }

        System.out.println("Згенерований вихідний масив:");
        printFormatted(originalArray);

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        List<Future<CopyOnWriteArraySet<Double>>> futureResults = new ArrayList<>();
        CopyOnWriteArraySet<Double> ResultSet = new CopyOnWriteArraySet<>();

        long startTime = System.nanoTime();
        int partSize = (int) Math.ceil((double) arraySize / NUM_THREADS);

        for (int i = 0; i < NUM_THREADS; i++) {
            int start = i * partSize;
            int end = Math.min((i + 1) * partSize, arraySize);
            if (start >= end) break;
            List<Double> subArray = originalArray.subList(start, end);
            futureResults.add(executor.submit(new SquareCalculator(subArray)));
        }

        System.out.println("\n...Завдання запущені, очікування результату...");
        for (Future<CopyOnWriteArraySet<Double>> future : futureResults) {
            try {
                CopyOnWriteArraySet<Double> partResult = future.get();
                if (future.isDone() && !future.isCancelled()) {
                    System.out.println(" - Отримано результат від одного з потоків.");
                    ResultSet.addAll(partResult);
                } else if (future.isCancelled()) {
                    System.out.println(" - Одне із завдань було скасовано.");
                }
            } catch (Exception e) {
                System.err.println("Помилка при отриманні результату: " + e.getMessage());
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;

        System.out.println("\n--- Успішно завершено ---");
        System.out.println("Кінцевий набір квадратів (CopyOnWriteArraySet):");

        printFormatted(ResultSet);

        System.out.println("\nЧас роботи програми: " + durationMs + " мс");
    }
    private static void printFormatted(Iterable<?> collection) {
        if (collection == null) {
            System.out.println("null");
            return;
        }
        int count = 0;
        int ELEMENTS_PER_LINE = 10;

        System.out.print("[");
        for (Object item : collection) {
            if (count > 0) {
                System.out.print(", ");
            }
            if (count % ELEMENTS_PER_LINE == 0 && count > 0) {
                System.out.print("\n ");
            }
            System.out.print(item);
            count++;
        }
        System.out.println("]");
    }
}