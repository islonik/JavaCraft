package my.javacraft.soap2rest.soap.service.order;

import my.javacraft.soap2rest.soap.generated.ds.ws.ServiceOrder;
import my.javacraft.soap2rest.soap.generated.ds.ws.ServiceOrderStatus;

public interface OrderService {

    String getServiceName();

    ServiceOrderStatus process(ServiceOrder serviceOrder);

}
