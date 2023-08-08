package my.javacraft.soap2rest.soap.service;

import my.javacraft.soap2rest.soap.generated.ds.ws.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EndpointService {

    @Autowired
    private AsyncService asyncService;
    @Autowired
    private DSRequestService dsRequestService;
    @Autowired
    private SyncService syncService;

    public DSResponse executeDsRequest(DSRequest dsRequest) {
        if (isAsync(dsRequest)) {
            return dsRequestService.getDSResponse(dsRequest, "501", "Async Service is not implemented yet!");
        }

        return dsRequestService.getDSResponse(dsRequest, "501", "Sync Service is not implemented yet!");
    }

    boolean isAsync(DSRequest dsRequest) {
        return Boolean.parseBoolean(dsRequest.getBody().getAsyncronousResponse());
    }

}
