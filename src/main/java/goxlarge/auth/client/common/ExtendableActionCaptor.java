package goxlarge.auth.client.common;


import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ExtendableActionCaptor implements ActionCaptor {

    private final ActionCaptor baseCaptor;
    private final List<ActionMutator> extensions;

    public ExtendableActionCaptor(ActionCaptor baseCaptor) {
        this(baseCaptor, new ArrayList<>());
    }

    public ExtendableActionCaptor(ActionCaptor baseCaptor, List<ActionMutator> baseExtensions) {
        this.baseCaptor = baseCaptor;
        this.extensions = Collections.unmodifiableList(baseExtensions);
    }

    public ExtendableActionCaptor extendedWith(ActionMutator additionalExtension) {
        List<ActionMutator> newExtensions = new LinkedList<>(extensions);
        newExtensions.add(additionalExtension);
        return new ExtendableActionCaptor(baseCaptor, newExtensions);
    }

    public ExtendableActionCaptor withDefaultDetails(Map<String, String> defaultDetails) {
        return extendedWith(a -> a.withDetails(defaultDetails));
    }

    public ExtendableActionCaptor withDefaultDetail(String dimension, String value) {
        return extendedWith(a -> a.withDetail(dimension, value));
    }

    public void capture(Action action) {
        extensions.forEach(extension -> extension.mutate(action));
        baseCaptor.capture(action);
    }
}