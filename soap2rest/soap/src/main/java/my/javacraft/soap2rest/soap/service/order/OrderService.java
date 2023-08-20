package my.javacraft.soap2rest.soap.service.order;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import my.javacraft.soap2rest.rest.api.Metric;
import my.javacraft.soap2rest.soap.generated.ds.ws.KeyValuesType;
import my.javacraft.soap2rest.soap.generated.ds.ws.ServiceOrder;
import my.javacraft.soap2rest.soap.generated.ds.ws.ServiceOrderStatus;
import my.javacraft.soap2rest.soap.generated.ds.ws.StatusType;
import org.springframework.http.HttpStatus;

public interface OrderService {

    default String getServiceName() {
        return this.getClass().getSimpleName();
    }

    default ServiceOrderStatus process(ServiceOrder serviceOrder) {
        ServiceOrderStatus sos = new ServiceOrderStatus();
        StatusType statusType = new StatusType();
        sos.setStatusType(statusType);
        try {
            sos = execProcess(serviceOrder);
        } catch (Exception e) {
            statusType.setCode(Integer.toString(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            statusType.setResult(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        }
        return sos;
    }

    ServiceOrderStatus execProcess(ServiceOrder serviceOrder) throws Exception;

    default Metric toMetric(List<KeyValuesType> paramsList) {
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

    default String toPath(List<KeyValuesType> paramsList) {
        return paramsList
                .stream()
                .filter(keyValuesType -> keyValuesType.getKey().equalsIgnoreCase("path"))
                .map(KeyValuesType::getValue)
                .map(String::trim)
                .findFirst()
                .orElse("");
    }

}
