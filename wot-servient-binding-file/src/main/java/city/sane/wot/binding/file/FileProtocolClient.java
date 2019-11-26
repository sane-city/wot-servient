package city.sane.wot.binding.file;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.content.Content;
import city.sane.wot.thing.form.Form;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Allows consuming Things via local files.
 */
public class FileProtocolClient implements ProtocolClient {
    final static Logger log = LoggerFactory.getLogger(FileProtocolClient.class);
    private static final Map<String, String> EXTENSION_TO_CONTENT_TYPE = new HashMap() {{
        put(".json", "application/json");
        put(".jsonld", "application/ld+json");
    }};

    @Override
    public CompletableFuture<Content> readResource(Form form) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path path = hrefToPath(form.getHref());
                String extension = pathToExtension(path);

                log.debug("Found extension '{}'", extension);
                String contentType = extensionToContentType(extension);
                if (contentType == null) {
                    log.warn("Cannot determine media type of '{}'", path);
                    contentType = "application/octet-stream";
                }

                byte[] body = Files.readAllBytes(path);
                Content content = new Content(contentType, body);
                return content;
            }
            catch (IOException e) {
                throw new CompletionException(new ProtocolClientException("Unable to read file '" + form.getHref() + "': " + e.getMessage()));
            }
        });
    }

    private Path hrefToPath(String href) {
        return Paths.get(URI.create(href).getSchemeSpecificPart());
    }

    private String pathToExtension(Path path) {
        String pathStr = path.toString();
        if (pathStr.contains(".")) {
            return pathStr.substring(pathStr.lastIndexOf("."));
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
}