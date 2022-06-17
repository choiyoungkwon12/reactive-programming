package org.example;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FruitInfo {
    private final List<String> distinctFruits;
    private final Map<String, Long> countFruits;

    public FruitInfo(List<String> distinctFruits, Map<String, Long> countFruits) {
        this.distinctFruits = distinctFruits;
        this.countFruits = countFruits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FruitInfo fruitInfo = (FruitInfo) o;
        return Objects.equals(distinctFruits, fruitInfo.distinctFruits) && Objects.equals(countFruits,
            fruitInfo.countFruits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(distinctFruits, countFruits);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FruitInfo{");
        sb.append("distinctFruits=").append(distinctFruits);
        sb.append(", countFruits=").append(countFruits);
        sb.append('}');
        return sb.toString();
    }
}
