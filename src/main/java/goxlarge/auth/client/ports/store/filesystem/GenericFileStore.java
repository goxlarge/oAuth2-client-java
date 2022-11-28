package goxlarge.auth.client.ports.store.filesystem;

import goxlarge.auth.client.model.AuthzCodeGrant;
import goxlarge.auth.client.model.ModelMapper;
import goxlarge.auth.client.ports.store.AuthorizationStore;
import goxlarge.auth.client.ports.utils.AuthorizationGrant;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

public class GenericFileStore<T> implements AuthorizationStore<T> {
    private static final String suffix = ".oAuth2";
    private final Supplier<Path> pathSupplier;
    private final Function<T, String> serializer;
    private final Function<String, T> deserializer;

    public GenericFileStore(Supplier<Path> pathSupplier, Function<T, String> serializer, Function<String, T> deserializer) {
        this.pathSupplier = pathSupplier;
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    @Override
    public void put(AuthorizationGrant<T> grant) {
        final String content;
        try {
            content = serializer.apply(grant.value());
        } catch (Exception e) {
            throw new StorageFailure("Failed to serialize object", e);
        }
        try {
            String suffix = grant.expires().toString();
            writeFileJava11(newPath(pathSupplier, suffix), content);
        } catch (Exception e) {
            throw new StorageFailure("Failed to store object to filesystem!", e);
        }
    }

    //create new file with expiration date as extension
    private Path newPath(Supplier<Path> pathSupplier, String suffix){
        Path currentPath = pathSupplier.get();
        return currentPath.resolveSibling(currentPath.getFileName() + "."+ suffix + suffix);
    }

    private static void writeFileJava11(Path path, String content) throws IOException {
        Files.writeString(path, content);
    }

    @Override
    public AuthorizationGrant<T> get() {
        try {
            Path theToken = findTokenPath(pathSupplier.get());
            final T value = deserializer.apply(Files.readString(theToken));

            final Instant expires = Instant.parse(expirationString(pathSupplier.get()));
            return new AuthorizationGrant<>(value, expires);
        } catch (Exception e) {
            throw new StorageFailure("Failed to deserialize object from filesystem!", e);
        }
    }

    private Path findTokenPath(Path path){
        String prefix = path.getName(path.getNameCount() -1).toString();
        File[] matches = path.getParent()
                .toFile()
                .listFiles((dir, name) -> name.startsWith(prefix) && name.endsWith(suffix));
        return matches[0].toPath();
    }

    private String expirationString(Path path){
        String prefix = path.getName(path.getNameCount() -1).toString();

        File[] matches = path.getParent()
                .toFile()
                .listFiles((dir, name) -> name.startsWith(prefix) && name.endsWith(".oAuth2"));
        String fullName = matches[0].getName();

       return fullName.replace(prefix+".","").replace(".oAuth2", "");
    }


    @Override
    public void destroy() {
        Path path = pathSupplier.get();
        String prefix = path.getName(path.getNameCount() -1).toString();
        File[] matches = path.getParent()
                .toFile()
                .listFiles((dir, name) -> name.startsWith(prefix) && name.endsWith(suffix));

        Arrays.stream(matches).map(File::toPath).forEach(d -> {
            try {
                Files.delete(d);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static GenericFileStore<String> ofAccessToken(Supplier<Path> file) {
        return new GenericFileStore<>(file, s -> s, s -> s);
    }

    public static GenericFileStore<AuthzCodeGrant> ofAuthzCodeGrant(Supplier<Path> file) {
        return new GenericFileStore<>(file,
                ModelMapper::serializeAuthzCodeGrant,
                ModelMapper::deserializeAuthzCodeGrant);
    }
}
