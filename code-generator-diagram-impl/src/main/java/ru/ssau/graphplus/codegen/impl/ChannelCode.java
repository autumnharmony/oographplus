/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl;

import com.google.common.base.Joiner;
import ru.ssau.graphplus.api.Node;

public class ChannelCode extends NodeCodeBase {
    private Iterable<?> parameters;
    private Iterable<?> states;

    public ChannelCode(Node node) {
        super(node);
    }

    /*
            channel = '~' ident [params] ['=' state {';' state}] '.'.
            state = ['+'] ident [ ('?'|'!') [rules] ].
            rules = rule { ',' rule }.
            rule = ident { ',' ident } '->' ident.
            process = '*' ident [params] ['=' ((ports [';' actions]) | actions) ] '.'.
            ports = port {';' port}.
            port = ident ':' ident ('?'|'!')[(rules ['|' '->' ident])|( '->' ident)].
            actions = action {';' action}.
            action = ['+'] [ident ':'] disjunction ['->' ([ident] '|' ident) | ident].
            disjunction = conjunction { '|' conjunction}.
            conjunction = call {'&' call}.
            call = ident '(' [args] ')'.
            args = ident ('?'|'!') ident {',' ident ('?'|'!') ident}.
            params = '<' ident {',' ident} '>'
    */


//    channel = '~' ident [params] ['=' state {';' state}] '.'.
//
//    ~Link =
//            +BEGIN ? argCos -> CALCCOS | argSin -> CALCSIN;
//    CALCCOS ! cos2 -> END; CALCSIN ! sin2 -> END.


    @Override
    public String getCode() {
        return "~" + node.getName() +
                (needParameters() ? "<" + Joiner.on(',').join(getParameters()) + ">" : "") +
                (needStates() ? "=" + Joiner.on(';').join(getStates()) : "") + ".";

    }

    private boolean needStates() {
        //TODO
        return false;
    }

    private boolean needParameters() {
        // TODO
        return false;
    }


    public Iterable<?> getParameters() {
        return parameters;
    }

    public Iterable<?> getStates() {
        return states;
    }
}
