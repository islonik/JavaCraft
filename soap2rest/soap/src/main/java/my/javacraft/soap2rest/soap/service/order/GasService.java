package my.javacraft.soap2rest.soap.service.order;

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
public class GasService implements OrderService  {

    @Autowired
    private HttpCallService httpCallService;

    public String getServiceName() {
        return this.getClass().getSimpleName();
    }

    public ServiceOrderStatus execProcess(ServiceOrder serviceOrder) throws Exception {
        ServiceOrderStatus sos = new ServiceOrderStatus();
        StatusType statusType = new StatusType();
        sos.setStatusType(statusType);

        String type = serviceOrder.getServiceType();
        String accountId = serviceOrder.getServiceOrderID();
        Metric metric = toMetric(serviceOrder.getParams());

        ResponseEntity<Metric> httpEntity;
        // http call
        if (type.startsWith(RequestMethod.PUT.toString())) {
            httpEntity = httpCallService.put("/api/v1/smart/%s/gas".formatted(accountId), metric);
            statusType.setCode(Integer.toString(httpEntity.getStatusCode().value()));
            statusType.setResult(Optional
                    .of(httpEntity)
                    .map(HttpEntity::getBody)
                    .map(Metric::toString)
                    .orElse(null)
            );
        } else {
            statusType.setCode(Integer.toString(HttpStatus.NOT_IMPLEMENTED.value()));
            statusType.setResult(HttpStatus.NOT_IMPLEMENTED.getReasonPhrase());
        }

        return sos;
    }

}
