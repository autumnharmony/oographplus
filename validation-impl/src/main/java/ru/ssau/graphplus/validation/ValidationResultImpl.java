/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.validation;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import ru.ssau.graphplus.api.DiagramElement;

import java.util.Collection;
import java.util.List;


public class ValidationResultImpl implements ValidationResult {


    private List<RuleResult> ruleResults;

    public ValidationResultImpl(List<RuleResult> ruleResults) {
        this.ruleResults = ruleResults;
    }

    @Override
    public Collection<Item> getItems() {
        Iterable<Item> transform = Iterables.transform(ruleResults, new Function<RuleResult, Item>() {
            @Override
            public Item apply(final RuleResult input) {
                return new Item() {
                    @Override
                    public String getShortDescription() {
                        return input.getDescription();
                    }

                    @Override
                    public String getFullDescription() {
                        return input.getDescription();
                    }

                    @Override
                    public DiagramElement getDiagramElement() {
                        return (DiagramElement) input.getItem();
                    }

                    public String toString(){
                        return input.getDescription();
                    }

                };
            }
        });
        return Lists.newArrayList(transform);
    }
}
