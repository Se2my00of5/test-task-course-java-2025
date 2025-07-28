import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class FileFilterUtility {

    public static void main(String[] args) {
        Configuration config = new Configuration();

        // Обработка аргументов командной строки
        try {
            parseArguments(args, config);

            System.out.println("Анализ аргументов командной строки завершен успешно.");

            // Выведение результата парсинга, для проверки
            printConfiguration(config);

            FileProcessor processor = new FileProcessor(config);
            processor.process();
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка в аргументах командной строки: " + e.getMessage());
            printUsage();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Произошла непредвиденная ошибка: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Разбирает массив аргументов командной строки и заполняет объект Configuration.
     *
     * @param args   Массив аргументов из метода main.
     * @param config Объект для заполнения настройками.
     * @throws IllegalArgumentException если аргументы некорректны.
     */
    private static void parseArguments(String[] args, Configuration config) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Не переданы аргументы. Укажите хотя бы один входной файл.");
        }

        Set<String> processedOptions = new HashSet<>();
        boolean hasDuplicatedFiles = false;

        String arg;
        for (int i = 0; i < args.length; i++) {
            arg = args[i];

            if (!arg.startsWith("-")) {
                // добавление файла, если дубль, то запоминаем
                if (!config.addInputFile(arg)) {
                    hasDuplicatedFiles = true;
                }
                continue;
            }

            if (!processedOptions.add(arg)) {
                throw new IllegalArgumentException("Опция " + arg + " не может быть указана дважды.");
            }

            switch (arg) {
                case "-o":
                    config.setOutputPath(Paths.get(extractOptionValue(args, i)));
                    i++;
                    break;
                case "-p":
                    config.setPrefix(extractOptionValue(args, i));
                    i++;
                    break;
                case "-a":
                    config.setAppendMode(true);
                    break;
                case "-s":
                    config.setStatMode(Configuration.StatMode.SHORT);
                    break;
                case "-f":
                    config.setStatMode(Configuration.StatMode.FULL);
                    break;
                default:
                    throw new IllegalArgumentException("Неизвестная опция: " + arg);
            }
        }

        if (processedOptions.contains("-s") && processedOptions.contains("-f")) {
            throw new IllegalArgumentException("Опции статистики -s и -f не могут использоваться вместе.");
        }


        if (config.getInputFiles().isEmpty()) {
            throw new IllegalArgumentException("Не указаны входные файлы для обработки.");
        }

        if (hasDuplicatedFiles) {
            System.out.println("Дублирующие файлы будут проверены единожды.\n");
        }
    }

    /**
     * Извлекает значение для опции на текущем индексе из массива аргументов.
     * Проверяет, что значение существует и не является другой опцией.
     */
    private static String extractOptionValue(String[] args, int index) {
        if (index + 1 >= args.length || args[index + 1].startsWith("-")) {
            throw new IllegalArgumentException("Опция " + args[index] + " требует указания значения.");
        }
        return args[index + 1];
    }

    /**
     * Выводит в консоль текущую конфигурацию утилиты.
     */
    private static void printConfiguration(Configuration config) {
        System.out.println("\n--- Текущая конфигурация ---");
        System.out.println("Путь для выходных файлов: " + config.getOutputPath().toAbsolutePath().normalize());
        System.out.println("Префикс для имен файлов: '" + config.getPrefix() + "'");
        System.out.println("Режим добавления в файлы: " + (config.isAppendMode() ? "Включен" : "Выключен (перезапись)"));
        System.out.println("Режим статистики: " + config.getStatMode());
        System.out.println("Входные файлы для обработки: " + config.getInputFiles());
        System.out.println("----------------------------\n");
    }

    /**
     * Выводит в консоль информацию о правильном использовании утилиты.
     */
    private static void printUsage() {
        System.err.println("\nИспользование: java FileFilterUtility [опции] file1 [file2 ...]");
        System.err.println("Опции:");
        System.err.println("  -o <путь>     Путь для сохранения файлов с результатами.");
        System.err.println("  -p <префикс>  Префикс для имен выходных файлов (например, 'result_').");
        System.err.println("  -a            Режим добавления данных в существующие файлы (по умолчанию перезапись).");
        System.err.println("  -s            Вывод краткой статистики.");
        System.err.println("  -f            Вывод полной статистики.");
    }
}