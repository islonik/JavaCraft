package my.javacraft.soap2rest.soap.service.order;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class MetricService {

    public static final String NAME = "MetricService";

    @Autowired
    private HttpCallService httpCallService;

    public ServiceOrderStatus process(ServiceOrder serviceOrder) throws JsonProcessingException {
        String type = serviceOrder.getServiceType();
        String accountId = serviceOrder.getServiceOrderID();
        Metric metric = toMetric(serviceOrder.getParams());

        ResponseEntity<Metric> httpEntity;
        // http call
        if (type.startsWith("gas")) {
            httpEntity = httpCallService.put("/api/v1/smart/%s/gas".formatted(accountId), metric);
        } else {
            httpEntity = httpCallService.put("/api/v1/smart/%s/electric".formatted(accountId), metric);
        }
        StatusType statusType = new StatusType();
        statusType.setResult("OK");
        statusType.setCode(Integer.toString(httpEntity.getStatusCode().value()));

        ServiceOrderStatus sos = new ServiceOrderStatus();
        sos.setStatusType(statusType);
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
