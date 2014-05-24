/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl;

import com.google.common.base.Strings;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.api.Node;

import java.util.List;
import java.util.Map;

public class ClientNodeCode extends NodeCodeBase {


    public ClientNodeCode(Node node, Map<Node, List<Link>> outgoingNodes, Map<Node, List<Link>> incomingNodes) {
        super(node, outgoingNodes, incomingNodes);
    }

    @Override
    public String getCode() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(node.getName());
        buffer.append("!");
        Link specialLink = null;
        int n = 0;
        for (Map.Entry<Node, List<Link>> entry : outgoingNodes.entrySet()) {

            for (Link link : entry.getValue()) {

                if (Strings.isNullOrEmpty(link.getName())) {
                    if (specialLink != null) {
                        throw new RuntimeException();
                    }
                    specialLink = link;
                } else {
                    buffer.append(link.getName()).append("->").append(entry.getKey().getName());
                }
            }

            if (n != outgoingNodes.size() - 1) {
                buffer.append("|");
            }
            n++;
        }


        if (specialLink != null) {
            buffer.append("|->").append(specialLink.getEndNode().getName());
        }

        buffer.append(";");


        return buffer.toString().replace("||", "|");

    }
}
