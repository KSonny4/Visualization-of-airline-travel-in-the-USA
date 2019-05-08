package core;

import model.Edge;
import model.Node;

public interface Observer {

    void updateProcessInfo(int iteration, int cycle);
    void finished(Node[]nodes, Edge[]edges);

}
