package my.javacraft.soap2rest.soap.service;

import lombok.extern.slf4j.Slf4j;
import my.javacraft.soap2rest.soap.generated.ds.ws.*;
import my.javacraft.soap2rest.soap.service.order.MetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SyncService {

    @Autowired
    private DSRequestService dsRequestService;

    @Autowired
    private MetricService metricService;

    public DSResponse syncProcess(DSRequest dsRequest) {
        try {
            ServiceOrder serviceOrder = dsRequest.getBody().getServiceOrder();
            // TODO: move to interface and auto search and injection
            if (serviceOrder.getServiceName().equalsIgnoreCase(MetricService.NAME)) {
                ServiceOrderStatus sos = metricService.process(serviceOrder);
                return dsRequestService.getOk(dsRequest, sos);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return dsRequestService.getDSResponse(dsRequest, "501", "Not implemented yet");
    }
}
