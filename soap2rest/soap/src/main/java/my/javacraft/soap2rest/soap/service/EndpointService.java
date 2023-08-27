package my.javacraft.soap2rest.soap.service;

import lombok.extern.slf4j.Slf4j;
import my.javacraft.soap2rest.soap.generated.ds.ws.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EndpointService {

    @Autowired
    private AsyncService asyncService;
    @Autowired
    private DSRequestService dsRequestService;
    @Autowired
    private SyncService syncService;

    public DSResponse executeDsRequest(DSRequest dsRequest) {
        try {
            if (isAsync(dsRequest)) {
                return dsRequestService.getDSResponse(dsRequest, "501", "Async Service is not implemented yet!");
            }

            return syncService.syncProcess(dsRequest);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return dsRequestService.getDSResponse(dsRequest, "500", "Internal Server Error");
    }

    boolean isAsync(DSRequest dsRequest) {
        return Boolean.parseBoolean(dsRequest.getBody().getAsyncronousResponse());
    }

}
