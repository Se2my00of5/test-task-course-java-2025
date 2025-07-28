package statistic;

public class ShortStatisticsCollector extends AbstractStatisticsCollector {

    @Override
    public String getStatistics() {
        return "Количество элементов: " + getCount();
    }
}