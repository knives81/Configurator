package com.dashboard.configurator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@AllArgsConstructor
@ToString
public class Testset {
    @Getter Integer id;
    @Getter String tag1;
    @Getter String tag2;
    @Getter String prj;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Testset testset = (Testset) o;
        return Objects.equals(tag1, testset.tag1) &&
                Objects.equals(tag2, testset.tag2) &&
                Objects.equals(prj, testset.prj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag1, tag2, prj);
    }
}
