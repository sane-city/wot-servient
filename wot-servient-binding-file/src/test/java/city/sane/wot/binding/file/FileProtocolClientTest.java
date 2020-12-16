package city.sane.wot.binding.file;

import city.sane.wot.content.Content;
import city.sane.wot.thing.form.Form;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.observers.LambdaObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.stubbing.answers.AnswersWithDelay;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.WatchService;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileProtocolClientTest {
    private Form form;
    private Function hrefToPath;
    private Path path;
    private Path directory;
    private FileSystem fileSystem;
    private WatchService watchService;

    @BeforeEach
    public void setUp() {
        form = mock(Form.class);
        hrefToPath = mock(Function.class);
        path = mock(Path.class);
        directory = mock(Path.class);
        fileSystem = mock(FileSystem.class);
        watchService = mock(WatchService.class);
    }

    @Test
    public void subscribeResourceShouldCreateWatchService() throws IOException {
        when(hrefToPath.apply(any())).thenReturn(path);
        when(path.getParent()).thenReturn(directory);
        when(directory.getFileSystem()).thenReturn(fileSystem);
        LambdaObserver<Content> observer = new LambdaObserver<>(n -> {
        }, e -> {
        }, () -> {
        }, s -> {
        });

        FileProtocolClient client = new FileProtocolClient(hrefToPath);
        client.observeResource(form).subscribe(observer);

        verify(directory, timeout(5 * 1000L)).register(any(), any());
    }

    @Test
    public void subscribeResourceShouldCloseWatchServiceWhenObserverIsDone() throws IOException, InterruptedException {
        when(hrefToPath.apply(any())).thenReturn(path);
        when(path.getParent()).thenReturn(directory);
        when(directory.getFileSystem()).thenReturn(fileSystem);
        when(fileSystem.newWatchService()).thenReturn(watchService);
        when(watchService.take()).thenAnswer(new AnswersWithDelay(5 * 1000L, new Returns(null)));

        FileProtocolClient client = new FileProtocolClient(hrefToPath);
        Disposable subscribe = client.observeResource(form).subscribe();

        // wait until subscriptions as been established
        verify(directory, timeout(5 * 1000L)).register(any(), any());

        subscribe.dispose();

        verify(watchService, timeout(5 * 1000L)).close();
    }
}