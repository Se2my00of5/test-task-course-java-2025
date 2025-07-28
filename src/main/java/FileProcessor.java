import statistic.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumMap;
import java.util.Map;

public class FileProcessor {

    private final Configuration config;

    private final Map<DataType, BufferedWriter> writers = new EnumMap<>(DataType.class);
    private final Map<DataType, StatisticsCollector> statisticsCollectors = new EnumMap<>(DataType.class);

    public FileProcessor(Configuration config) {
        this.config = config;
        initializeStatistics();
    }

    /**
     * Инициализирует сборщики статистики в зависимости от настроек.
     */
    private void initializeStatistics() {
        if (config.getStatMode() == Configuration.StatMode.NONE) {
            return;
        }

        if (config.getStatMode() == Configuration.StatMode.SHORT) {
            for (DataType type : DataType.values()) {
                statisticsCollectors.put(type, new ShortStatisticsCollector());
            }
        } else if (config.getStatMode() == Configuration.StatMode.FULL) {
            statisticsCollectors.put(DataType.INTEGER, new NumberStatisticsCollector());
            statisticsCollectors.put(DataType.FLOAT, new NumberStatisticsCollector());
            statisticsCollectors.put(DataType.STRING, new StringStatisticsCollector());
        }
    }

    /**
     * Главный метод, запускающий процесс обработки.
     */
    public void process() {
        System.out.println("Начинается обработка файлов...");

        try {
            for (String inputFileName : config.getInputFiles()) {
                processSingleFile(inputFileName);
            }
            System.out.println("Обработка файлов завершена.");

            printStatistics();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            closeWriters();
        }
    }

    /**
     * Обрабатывает один входной файл.
     *
     * @param fileName путь к файлу для обработки.
     */
    private void processSingleFile(String fileName) throws IOException {
        Path filePath = Paths.get(fileName);

        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            System.err.println("  - Ошибка: Файл '" + fileName + "' не найден или недоступен для чтения. Файл пропущен.");
            return;
        }

        System.out.println("  - Чтение файла: " + fileName);
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) { // Пропускаем пустые строки
                    processLine(line);
                }
            }
        } catch (IOException e) {
            System.err.println("  - Ошибка при чтении файла '" + fileName + "': " + e.getMessage() + ". Обработка продолжается.");
        }
    }

    /**
     * Определяет тип данных в строке и делегирует запись и сбор статистики.
     */
    private void processLine(String line) throws IOException {
        try {
            Long.parseLong(line);
            writeData(DataType.INTEGER, line);
            return;
        } catch (NumberFormatException ignore) {
        }

        try {
            Double.parseDouble(line);
            writeData(DataType.FLOAT, line);
            return;
        } catch (NumberFormatException ignore) {
        }

        writeData(DataType.STRING, line);
    }

    /**
     * Записывает данные в соответствующий файл.
     *
     * @param type Тип данных.
     * @param data Строка для записи.
     */
    private void writeData(DataType type, String data) throws IOException {
        try {
            BufferedWriter writer = getWriterForType(type);
            writer.write(data);
            writer.newLine();

            // Если сборщик для этого типа существует, собираем статистику
            if (statisticsCollectors.containsKey(type)) {
                statisticsCollectors.get(type).collect(data);
            }
        } catch (IOException e) {
            throw new IOException("Критическая ошибка: не удалось записать данные в файл для типа " + type + ". " + e.getMessage());
            //System.err.println("Критическая ошибка: не удалось записать данные в файл для типа " + type + ". " + e.getMessage());
        }
    }

    /**
     * Выводит собранную статистику в консоль.
     */
    private void printStatistics() {
        if (config.getStatMode() == Configuration.StatMode.NONE) {
            return;
        }
        System.out.println("\n--- Статистика ---");
        for (Map.Entry<DataType, StatisticsCollector> entry : statisticsCollectors.entrySet()) {
            DataType type = entry.getKey();
            AbstractStatisticsCollector collector = (AbstractStatisticsCollector) entry.getValue();

            System.out.println("\nСтатистика для типа: " + type.name());
            System.out.println(collector.getStatistics());
        }
        System.out.println("--------------------");
    }

    /**
     * Получает (или создает при первом обращении) BufferedWriter для заданного типа данных.
     */
    private BufferedWriter getWriterForType(DataType type) throws IOException {
        if (writers.containsKey(type)) {
            return writers.get(type);
        }

        Path outputDir = config.getOutputPath();

        Files.createDirectories(outputDir);

        String fileName = config.getPrefix() + type.getDefaultFileName();
        Path filePath = outputDir.resolve(fileName);

        //System.out.println("Создан выходной файл: " + filePath.toAbsolutePath().normalize());

        // Определяем режим открытия файла: дозапись или перезапись
        StandardOpenOption openOption = config.isAppendMode() ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING;

        BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.CREATE, openOption);
        writers.put(type, writer);
        return writer;
    }

    /**
     * Корректно закрывает все открытые писатели.
     */
    private void closeWriters() {
        for (Map.Entry<DataType, BufferedWriter> entry : writers.entrySet()) {
            try {
                entry.getValue().close();
            } catch (IOException e) {
                System.err.println("Ошибка при закрытии файла для типа " + entry.getKey() + ": " + e.getMessage());
            }
        }
    }
}