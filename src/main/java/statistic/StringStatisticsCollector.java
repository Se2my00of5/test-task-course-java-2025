package statistic;

public class StringStatisticsCollector extends AbstractStatisticsCollector {
    private int minLength = -1;
    private int maxLength = 0;

    @Override
    public void collect(String data) {
        super.collect(data); // Увеличиваем счетчик count
        int currentLength = data.length();

        if (minLength == -1 || currentLength < minLength) {
            minLength = currentLength;
        }
        if (currentLength > maxLength) {
            maxLength = currentLength;
        }
    }

    @Override
    public String getStatistics() {
        if (getCount() == 0) {
            return "Статистика по строкам не собрана (элементов нет).";
        }
        return String.format(
                "Количество строк: %d%n" +
                        "Длина самой короткой строки: %d%n" +
                        "Длина самой длинной строки: %d",
                getCount(), minLength, maxLength
        );
    }
}