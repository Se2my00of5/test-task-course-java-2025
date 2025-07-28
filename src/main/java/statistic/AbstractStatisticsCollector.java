package statistic;

public abstract class AbstractStatisticsCollector implements StatisticsCollector {
    protected long count = 0;

    @Override
    public void collect(String data) {
        this.count++;
    }

    protected long getCount() {
        return count;
    }
}