package statistic;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberStatisticsCollector extends AbstractStatisticsCollector {
    private BigDecimal min = null;
    private BigDecimal max = null;
    private BigDecimal sum = BigDecimal.ZERO;

    @Override
    public void collect(String data) {
        super.collect(data); // Увеличиваем счетчик count
        BigDecimal value = new BigDecimal(data);

        sum = sum.add(value);

        if (max == null || value.compareTo(max) > 0) {
            max = value;
        }
        if (min == null || value.compareTo(min) < 0) {
            min = value;
        }
    }

    @Override
    public String getStatistics() {
        if (getCount() == 0) {
            return "Статистика по числам не собрана (элементов нет).";
        }

        BigDecimal average = sum.divide(BigDecimal.valueOf(getCount()), 5, RoundingMode.HALF_UP);

        return String.format(
                "Количество элементов: %d%n" +
                        "Минимальное значение: %s%n" +
                        "Максимальное значение: %s%n" +
                        "Сумма: %s%n" +
                        "Среднее: %s",
                getCount(), min.toPlainString(), max.toPlainString(), sum.toPlainString(), average.toPlainString()
        );
    }
}