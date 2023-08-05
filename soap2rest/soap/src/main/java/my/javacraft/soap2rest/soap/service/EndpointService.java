package my.javacraft.soap2rest.soap.service;

import java.util.Optional;
import my.javacraft.soap2rest.soap.generated.ds.ws.DSRequest;
import my.javacraft.soap2rest.soap.generated.ds.ws.DSResponse;
import my.javacraft.soap2rest.soap.generated.ds.ws.ServiceOrderStatus;
import my.javacraft.soap2rest.soap.generated.ds.ws.StatusType;
import org.springframework.stereotype.Service;

@Service
public class EndpointService {

    public DSResponse getDSResponse(DSRequest dsRequest, String code, String message) {
        DSResponse dsResponse = new DSResponse();
        dsResponse.setHeader(dsRequest.getHeader());

        DSResponse.Body body = new DSResponse.Body();
        dsResponse.setBody(body);

        ServiceOrderStatus sos = new ServiceOrderStatus();
        body.setServiceOrderStatus(sos);

        Optional<String> orderId = Optional.ofNullable(dsRequest
                .getBody()
                .getServiceOrder()
                .getServiceOrderID()
        );
        orderId.ifPresent(sos::setServiceOrderID);

        StatusType statusType = new StatusType();
        sos.setStatusType(statusType);
        statusType.setCode(code);
        statusType.setDesc(message);

        return dsResponse;
    }
}
