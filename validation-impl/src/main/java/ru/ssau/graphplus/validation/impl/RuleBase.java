package ru.ssau.graphplus.validation.impl;

import ru.ssau.graphplus.Global;
import ru.ssau.graphplus.validation.Rule;
import ru.ssau.graphplus.validation.RuleResult;

import java.util.Locale;
import java.util.ResourceBundle;

public abstract class RuleBase<T> implements Rule<T> {

    protected RuleError error(T item) {
        return error(item, getClass().getSimpleName());
    }

    protected RuleError warning(T item) {
        return warning(item, getClass().getSimpleName());
    }

    protected RuleError warning(T item, String key) {
        String simpleName = null;
        try {
            ResourceBundle message = ResourceBundle.getBundle("messages", new Locale(Global.locale));
            simpleName = getClass().getSimpleName();
            return new RuleError<>(message.getString(key), item, RuleResult.Type.Warning);
        } catch (Exception e) {
            return new RuleError<>(simpleName, item);
        }
    }

    protected RuleError error(T item, String key) {
        String simpleName = null;
        try {
            ResourceBundle message = ResourceBundle.getBundle("messages", new Locale(Global.locale));
            simpleName = getClass().getSimpleName();
            return new RuleError<>(message.getString(key), item);
        } catch (Exception e) {
            return new RuleError<>(simpleName, item);
        }
    }
}
