package goxlarge.auth.client.common;


public interface ActionMutator<T extends Action<T>> {
    void mutate(T action);
}