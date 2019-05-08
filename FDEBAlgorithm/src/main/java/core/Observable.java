package core;

public interface Observable {

    void registerObserver(Observer observer);
    void notifyObservers(int iteration, int cycle, boolean finished);
}
