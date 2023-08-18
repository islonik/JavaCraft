package my.javacraft.soap2rest.soap.service;

import java.util.Optional;
import my.javacraft.soap2rest.soap.generated.ds.ws.*;
import my.javacraft.soap2rest.soap.generated.ds.ws.DSRequest.Body;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

@Service
public class DSRequestService {

    DSResponse getOk(DSRequest dsRequest, ServiceOrderStatus sos) {
        DSResponse dsResponse = new DSResponse();
        dsResponse.setHeader(dsRequest.getHeader());

        DSResponse.Body body = new DSResponse.Body();
        dsResponse.setBody(body);

        body.setServiceOrderStatus(sos);

        return dsResponse;
    }

    DSResponse getDSResponse(DSRequest dsRequest, String code, String message) {
        DSResponse dsResponse = new DSResponse();
        dsResponse.setHeader(dsRequest.getHeader());

        DSResponse.Body body = new DSResponse.Body();
        dsResponse.setBody(body);

        ServiceOrderStatus sos = new ServiceOrderStatus();
        body.setServiceOrderStatus(sos);

        Optional<String> orderId = Optional.of(dsRequest)
                .map(DSRequest::getBody)
                .map(Body::getServiceOrder)
                .map(ServiceOrder::getServiceOrderID);
        orderId.ifPresent(sos::setServiceOrderID);

        StatusType statusType = new StatusType();
        sos.setStatusType(statusType);
        statusType.setCode(code);
        statusType.setDesc(message);

        return dsResponse;
    }

}
