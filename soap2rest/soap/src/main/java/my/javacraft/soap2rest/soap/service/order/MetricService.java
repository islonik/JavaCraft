package my.javacraft.soap2rest.soap.service.order;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import my.javacraft.soap2rest.rest.api.Metric;
import my.javacraft.soap2rest.soap.generated.ds.ws.KeyValuesType;
import my.javacraft.soap2rest.soap.generated.ds.ws.ServiceOrder;
import my.javacraft.soap2rest.soap.generated.ds.ws.ServiceOrderStatus;
import my.javacraft.soap2rest.soap.generated.ds.ws.StatusType;
import my.javacraft.soap2rest.soap.service.HttpCallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class MetricService implements OrderService {

    public static final String NAME = "MetricService";

    @Autowired
    private HttpCallService httpCallService;

    public String getServiceName() {
        return NAME;
    }

    public ServiceOrderStatus process(ServiceOrder serviceOrder) {
        StatusType statusType = new StatusType();
        statusType.setResult(HttpStatus.OK.getReasonPhrase());

        ServiceOrderStatus sos = new ServiceOrderStatus();
        sos.setStatusType(statusType);

        try {
            String type = serviceOrder.getServiceType();
            String accountId = serviceOrder.getServiceOrderID();
            Metric metric = toMetric(serviceOrder.getParams());

            ResponseEntity<Metric> httpEntity;
            // http call
            if (type.startsWith("gas")) {
                httpEntity = httpCallService.put("/api/v1/smart/%s/gas".formatted(accountId), metric);
                statusType.setCode(Integer.toString(httpEntity.getStatusCode().value()));
            } else if (type.startsWith("electric")) {
                httpEntity = httpCallService.put("/api/v1/smart/%s/electric".formatted(accountId), metric);
                statusType.setCode(Integer.toString(httpEntity.getStatusCode().value()));
            } else {
                statusType.setResult(HttpStatus.NOT_IMPLEMENTED.getReasonPhrase());
                statusType.setCode(Integer.toString(HttpStatus.NOT_IMPLEMENTED.value()));
            }
        } catch (Exception e) {
            statusType.setResult(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
            statusType.setCode(Integer.toString(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
        return sos;
    }

    private Metric toMetric(List<KeyValuesType> paramsList) {
        Metric metric = new Metric();
        for (KeyValuesType key : paramsList) {
            switch (key.getKey()) {
                case "meterId" -> metric.setMeterId(Long.parseLong(key.getValue()));
                case "reading" -> metric.setReading(new BigDecimal(key.getValue()));
                case "date" -> metric.setDate(Date.valueOf(key.getValue()));
            }
        }
        return metric;
    }


}
