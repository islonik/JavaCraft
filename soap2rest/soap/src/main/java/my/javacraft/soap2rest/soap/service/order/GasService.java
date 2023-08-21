package my.javacraft.soap2rest.soap.service.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.Optional;
import my.javacraft.soap2rest.rest.api.Metric;
import my.javacraft.soap2rest.soap.generated.ds.ws.ServiceOrder;
import my.javacraft.soap2rest.soap.generated.ds.ws.StatusType;
import my.javacraft.soap2rest.soap.service.HttpCallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class GasService implements OrderService  {

    @Autowired
    private HttpCallService httpCallService;

    @Override
    public void put(ServiceOrder serviceOrder, StatusType statusType) throws JsonProcessingException {
        String accountId = serviceOrder.getServiceOrderID();

        Metric metric = toMetric(serviceOrder.getParams());

        ResponseEntity<Metric> httpEntity = httpCallService.put("/api/v1/smart/%s/gas".formatted(accountId), Metric.class, metric);

        statusType.setCode(Integer.toString(httpEntity.getStatusCode().value()));
        statusType.setResult(Optional
                .of(httpEntity)
                .map(HttpEntity::getBody)
                .map(Metric::toString)
                .orElse(null)
        );
    }

    @Override
    public void delete(ServiceOrder serviceOrder, StatusType statusType) {
        String accountId = serviceOrder.getServiceOrderID();

        ResponseEntity<Boolean> httpEntity =
                httpCallService.delete("/api/v1/smart/%s/gas".formatted(accountId));

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
            ResponseEntity<Metric> httpEntity =
                    httpCallService.get("/api/v1/smart/%s/gas/latest".formatted(accountId), Metric.class);

            statusType.setCode(Integer.toString(httpEntity.getStatusCode().value()));
            statusType.setResult(Optional
                    .of(httpEntity)
                    .map(HttpEntity::getBody)
                    .map(Metric::toString)
                    .orElse(null)
            );
        } else {
            ResponseEntity<Object> httpEntity =
                    httpCallService.get("/api/v1/smart/%s/gas".formatted(accountId), Object.class);

            statusType.setCode(Integer.toString(httpEntity.getStatusCode().value()));

            List<Metric> metricList = (List<Metric>) httpEntity.getBody();
            statusType.setResult(Optional
                    .ofNullable(metricList)
                    .map(Object::toString)
                    .orElse("")
            );
        }
    }

}
