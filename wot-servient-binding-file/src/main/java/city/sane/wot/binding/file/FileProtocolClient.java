package city.sane.wot.binding.file;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.content.Content;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.observer.Observer;
import city.sane.wot.thing.observer.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Allows consuming Things via local files.
 */
public class FileProtocolClient implements ProtocolClient {
    private static final Logger log = LoggerFactory.getLogger(FileProtocolClient.class);
    private static final Map<String, String> EXTENSION_TO_CONTENT_TYPE = Map.of(
        ".json", "application/json",
        ".jsonld", "application/ld+json"
    );

    @Override
    public CompletableFuture<Content> readResource(Form form) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path path = hrefToPath(form.getHref());
                return getContentFromPath(path);
            }
            catch (IOException e) {
                throw new CompletionException(new ProtocolClientException("Unable to read file '" + form.getHref() + "': " + e.getMessage()));
            }
        });
    }

    @Override
    public CompletableFuture<Content> writeResource(Form form, Content content) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path path = hrefToPath(form.getHref());

                Files.write(path, content.getBody(), StandardOpenOption.CREATE);

                return Content.EMPTY_CONTENT;
            }
            catch (IOException e) {
                throw new CompletionException(new ProtocolClientException("Unable to write file '" + form.getHref() + "': " + e.getMessage()));
            }
        });
    }

    @Override
    public CompletableFuture<Subscription> subscribeResource(Form form, Observer<Content> observer) {
        Path path = hrefToPath(form.getHref());
        Path directory = path.getParent();

        try (final WatchService watchService = directory.getFileSystem().newWatchService()) {
            directory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            CompletableFuture.runAsync(() -> {
                // We obtain the file system of the Path
                FileSystem fileSystem = directory.getFileSystem();

                // We create the new WatchService using the try-with-resources block
                try (WatchService service = fileSystem.newWatchService()) {
                    // We watch for modification events
                    directory.register(service, ENTRY_MODIFY, ENTRY_DELETE, ENTRY_CREATE);

                    // Start the infinite polling loop
                    while (true) {
                        // Wait for the next event
                        WatchKey watchKey = service.take();

                        for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                            // Get the type of the event
                            WatchEvent.Kind<?> kind = watchEvent.kind();

                            if (kind == ENTRY_MODIFY || kind == ENTRY_DELETE || kind == ENTRY_CREATE) {
                                Path watchEventPath = (Path) watchEvent.context();

                                // Call this if the right file is involved
                                if (path.getFileName().equals(watchEventPath)) {
                                    Content content = getContentFromPath(path);
                                    observer.next(content);
                                }
                            }
                        }

                        if (!watchKey.reset()) {
                            break;
                        }
                    }

                    observer.complete();
                }
                catch (IOException | InterruptedException e) {
                    observer.error(new ProtocolClientException(e));
                }
            });

            Subscription subscription = new Subscription(() -> {
                try {
                    watchService.close();
                }
                catch (IOException e) {
                    // ignore
                }
            });

            return CompletableFuture.completedFuture(subscription);
        }
        catch (IOException e) {
            return CompletableFuture.failedFuture(new ProtocolClientException(e));
        }
    }

    private Path hrefToPath(String href) {
        return Paths.get(URI.create(href).getSchemeSpecificPart());
    }

    private String pathToExtension(Path path) {
        String pathStr = path.toString();
        if (pathStr.contains(".")) {
            return pathStr.substring(pathStr.lastIndexOf('.'));
        }
        else {
            return "";
        }
    }

    private String extensionToContentType(String extension) {
        String contentType;

        // try java's FileNameMap first
        contentType = URLConnection.guessContentTypeFromName(extension);
        if (contentType == null) {
            // use own mapping as fallback
            contentType = EXTENSION_TO_CONTENT_TYPE.get(extension);
            if (contentType == null) {
                // fallback
                log.warn("FileClient cannot determine media type for extension {}", extension);
                contentType = "application/octet-stream";
            }
        }

        return contentType;
    }

    private Content getContentFromPath(Path path) throws IOException {
        String extension = pathToExtension(path);

        log.debug("Found extension '{}'", extension);
        String contentType = extensionToContentType(extension);
        if (contentType == null) {
            log.warn("Cannot determine media type of '{}'", path);
            contentType = "application/octet-stream";
        }

        byte[] body = Files.readAllBytes(path);
        return new Content(contentType, body);
    }
}