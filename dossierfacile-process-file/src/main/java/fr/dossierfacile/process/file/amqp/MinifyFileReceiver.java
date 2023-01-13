package fr.dossierfacile.process.file.amqp;

import com.google.gson.Gson;
import fr.dossierfacile.process.file.amqp.model.TenantModel;
import fr.dossierfacile.process.file.service.interfaces.MinifyFile;
import io.sentry.Sentry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@AllArgsConstructor
public class MinifyFileReceiver {
    private static final String EXCEPTION = "Sentry ID Exception: ";
    private final Gson gson;
    private final MinifyFile minifyFile;
    @RabbitListener(queues = "${rabbitmq.queue.file.minify}", containerFactory = "retryContainerFactory")
    public void processFileTax(Map<String, String> item) {
        try {
            log.info("Receive minify file");
            Long fileId = Long.valueOf(item.get("id"));
            log.info("File ID received [" + fileId + "]");
            minifyFile.process(fileId);

        } catch (Exception e) {
            log.error(EXCEPTION + Sentry.captureException(e));
            log.error(e.getMessage(), e.getCause());
            throw e;
        }
    }
}
