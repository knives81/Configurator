package com.dashboard.configurator;

import com.dashboard.commondashboard.Target;
import lombok.AllArgsConstructor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@AllArgsConstructor
public class TestsetHelper {
    private List<Testset> testsets;
    private HashMap<String, Target> prjToTarget;


    public List<Integer> getIds(String AO, String cycle) {
        List<Integer> ids = testsets.stream()
                .filter(t -> t.getTag1().equals(AO))
                .filter(t -> t.getTag2().equals(cycle))
                .map(Testset::getId)
                .collect(Collectors.toList());
        return ids;
    }

    public Set<Testset> getUniqueTestsetsNoId() {
        return new LinkedHashSet<>(testsets);
    }


    public List<String> getAOs() {
        List<String> AOs = testsets.stream()
                .filter(distinctByKey(Testset::getTag1))
                .map(Testset::getTag1)
                .map(ao -> ao.toUpperCase())
                .collect(Collectors.toList());
        System.out.println(AOs);
        return AOs;
    }

    Target getTargetPerAOFromTestset(String AO) {
        String prj = testsets.stream()
                .filter(t -> t.getTag1().equals(AO))
                .map(Testset::getPrj)
                .findFirst().orElseThrow(() -> new RuntimeException("No Prj Found"));

        Target target = prjToTarget.get(prj);
        if(target==null) {
            throw new RuntimeException("No project found in target sheet");
        }
        return target;
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

}
