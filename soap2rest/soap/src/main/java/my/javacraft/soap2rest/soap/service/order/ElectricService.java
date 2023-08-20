package my.javacraft.soap2rest.soap.service.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.Optional;
import my.javacraft.soap2rest.rest.api.Metric;
import my.javacraft.soap2rest.soap.generated.ds.ws.ServiceOrder;
import my.javacraft.soap2rest.soap.generated.ds.ws.ServiceOrderStatus;
import my.javacraft.soap2rest.soap.generated.ds.ws.StatusType;
import my.javacraft.soap2rest.soap.service.HttpCallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

@Service
public class ElectricService implements OrderService {

    @Autowired
    private HttpCallService httpCallService;

    public ServiceOrderStatus execProcess(ServiceOrder serviceOrder) throws Exception {
        ServiceOrderStatus sos = new ServiceOrderStatus();
        StatusType statusType = new StatusType();
        sos.setStatusType(statusType);

        String type = serviceOrder.getServiceType();
        String accountId = serviceOrder.getServiceOrderID();

        // http call
        if (type.equalsIgnoreCase(RequestMethod.PUT.toString())) {
            put(serviceOrder, accountId, statusType);
        } else if (type.equalsIgnoreCase(RequestMethod.DELETE.toString())) {
            delete(accountId, statusType);
        } else if (type.equalsIgnoreCase(RequestMethod.GET.toString())) {
            get(serviceOrder, accountId, statusType);
        } else {
            statusType.setCode(Integer.toString(HttpStatus.NOT_IMPLEMENTED.value()));
            statusType.setResult(HttpStatus.NOT_IMPLEMENTED.getReasonPhrase());
        }

        return sos;
    }

    private void put(ServiceOrder serviceOrder, String accountId, StatusType statusType) throws JsonProcessingException {
        Metric metric = toMetric(serviceOrder.getParams());

        ResponseEntity<Metric> httpEntity =
                httpCallService.put("/api/v1/smart/%s/electric".formatted(accountId), metric);

        statusType.setCode(Integer.toString(httpEntity.getStatusCode().value()));
        statusType.setResult(Optional
                .of(httpEntity)
                .map(HttpEntity::getBody)
                .map(Metric::toString)
                .orElse(null)
        );
    }

    private void delete(String accountId, StatusType statusType) {
        ResponseEntity<Boolean> httpEntity =
                httpCallService.delete("/api/v1/smart/%s/electric".formatted(accountId));

        statusType.setCode(Integer.toString(httpEntity.getStatusCode().value()));
        statusType.setResult(Optional
                .of(httpEntity)
                .map(HttpEntity::getBody)
                .map(Object::toString)
                .orElse(null)
        );
    }

    private void get(ServiceOrder serviceOrder, String accountId, StatusType statusType) {
        String path = toPath(serviceOrder.getParams());

        if (path.equalsIgnoreCase("/latest")) {
            ResponseEntity<Metric> httpEntity =
                    httpCallService.get("/api/v1/smart/%s/electric%s".formatted(accountId, path), Metric.class);

            statusType.setCode(Integer.toString(httpEntity.getStatusCode().value()));
            statusType.setResult(Optional
                    .of(httpEntity)
                    .map(HttpEntity::getBody)
                    .map(Metric::toString)
                    .orElse(null)
            );
        } else {
            ResponseEntity<Object> httpEntity =
                    httpCallService.get("/api/v1/smart/%s/electric%s".formatted(accountId, path), Object.class);

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
