package ru.ssau.graphplus.link;

import ru.ssau.graphplus.node.NodeBase;

/**
 * Created by 1 on 03.06.14.
 */
public interface LinkAdjuster {

    void adjustLink(LinkBase linkBase, NodeBase from, NodeBase to);
}
