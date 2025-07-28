package statistic;

public interface StatisticsCollector {
    void collect(String data);

    /**
     * Возвращает собранную статистику в виде форматированной строки для вывода в консоль.
     *
     * @return строка со статистикой.
     */
    String getStatistics();
}
