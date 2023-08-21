package my.javacraft.soap2rest.soap.service.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Optional;
import my.javacraft.soap2rest.rest.api.Metrics;
import my.javacraft.soap2rest.soap.generated.ds.ws.ServiceOrder;
import my.javacraft.soap2rest.soap.generated.ds.ws.StatusType;
import my.javacraft.soap2rest.soap.service.HttpCallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class SmartService implements OrderService {

    @Autowired
    private HttpCallService httpCallService;

    @Override
    public void put(ServiceOrder serviceOrder, StatusType statusType) throws JsonProcessingException {
        String accountId = serviceOrder.getServiceOrderID();

        Metrics metrics = toMetrics(serviceOrder.getParams());
        metrics.setAccountId(Long.parseLong(accountId));

        ResponseEntity<Boolean> httpEntity = httpCallService.put("/api/v1/smart/%s".formatted(accountId), Boolean.class, metrics);

        statusType.setCode(Integer.toString(httpEntity.getStatusCode().value()));
        statusType.setResult(Optional
                .of(httpEntity)
                .map(HttpEntity::getBody)
                .map(Object::toString)
                .orElse(null)
        );
    }

    @Override
    public void delete(ServiceOrder serviceOrder, StatusType statusType) {
        String accountId = serviceOrder.getServiceOrderID();

        ResponseEntity<Boolean> httpEntity =
                httpCallService.delete("/api/v1/smart/%s".formatted(accountId));

        statusType.setCode(Integer.toString(httpEntity.getStatusCode().value()));
        statusType.setResult(Optional
                .of(httpEntity)
                .map(HttpEntity::getBody)
                .map(Object::toString)
                .orElse(null)
        );
    }

    @Override
    public void get(ServiceOrder serviceOrder, StatusType statusType) {
        String accountId = serviceOrder.getServiceOrderID();

        String path = toPath(serviceOrder.getParams());

        if (path.equalsIgnoreCase("/latest")) {
            ResponseEntity<Metrics> httpEntity =
                    httpCallService.get("/api/v1/smart/%s/latest".formatted(accountId), Metrics.class);

            statusType.setCode(Integer.toString(httpEntity.getStatusCode().value()));
            statusType.setResult(Optional
                    .of(httpEntity)
                    .map(HttpEntity::getBody)
                    .map(Metrics::toString)
                    .orElse(null)
            );
        } else {
            ResponseEntity<Metrics> httpEntity =
                    httpCallService.get("/api/v1/smart/%s".formatted(accountId), Metrics.class);

            statusType.setCode(Integer.toString(httpEntity.getStatusCode().value()));
            statusType.setResult(Optional
                    .of(httpEntity)
                    .map(HttpEntity::getBody)
                    .map(Metrics::toString)
                    .orElse(null)
            );
        }
    }
}
