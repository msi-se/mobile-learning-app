package de.htwg_konstanz.mobilelearning.helper;

import java.util.List;
import java.util.ArrayList;

public class Analytics {
    public Double avg;
    public Double min;
    public Double max;
    public Double median;
    public Integer count;

    public Analytics() {
        this.avg = 0.0;
        this.min = 0.0;
        this.max = 0.0;
        this.median = 0.0;
        this.count = 0;
    }

    public void update(List<String> values) {
        
        // reset values
        if (values == null || values.size() == 0) {
            this.avg = 0.0;
            this.min = 0.0;
            this.max = 0.0;
            this.median = 0.0;
            this.count = 0;
            return;
        }

        // convert values to double
        List<Double> valuesAsDouble = new ArrayList<Double>();
        values.forEach(valueAsString -> {
            try {
                valuesAsDouble.add(Double.parseDouble(valueAsString));
            } catch (NumberFormatException e) {
                // ignore
            }
        });

        // if everything is a string, update the count and return
        if (valuesAsDouble.size() == 0) {
            this.count = values.size();
            return;
        }

        // update
        this.count = values.size();
        this.min = valuesAsDouble.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        this.max = valuesAsDouble.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        this.avg = valuesAsDouble.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        if (valuesAsDouble.size() % 2 == 0) {
            this.median = (valuesAsDouble.get(valuesAsDouble.size() / 2) + valuesAsDouble.get(valuesAsDouble.size() / 2 - 1)) / 2;
        } else {
            this.median = valuesAsDouble.get(valuesAsDouble.size() / 2);
        }
    }

    public Double getAvg() {
        return this.avg;
    }

    public Double getMin() {
        return this.min;
    }

    public Double getMax() {
        return this.max;
    }

    public Double getMedian() {
        return this.median;
    }

    public Integer getCount() {
        return this.count;
    }

    public Analytics deepCopy() {
        Analytics copy = new Analytics();
        copy.avg = this.avg;
        copy.min = this.min;
        copy.max = this.max;
        copy.median = this.median;
        copy.count = this.count;
        return copy;
    }

}
