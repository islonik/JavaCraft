package my.javacraft.ses.simulator;

import my.javacraft.ses.simulator.flow.Creator;
import my.javacraft.ses.simulator.flow.Reporter;
import my.javacraft.ses.simulator.flow.Validator;
import my.javacraft.ses.simulator.flow.Worker;
import my.javacraft.ses.simulator.model.Task;
import my.javacraft.ses.simulator.services.FinanceService;
import my.javacraft.ses.simulator.services.QueueServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by nikilipa on 7/23/16.
 */
@Component
public class WorkerLauncher {

    private final FinanceService financeService;
    private final QueueServices queueServices;
    private final ExecutorService executorService;
    private final PriorityBlockingQueue<Task> fromCreator;
    private final PriorityBlockingQueue<Task> fromValidator;

    @Autowired
    public WorkerLauncher(FinanceService financeService, QueueServices queueServices) {
        this.financeService = financeService;
        this.queueServices = queueServices;

        this.executorService = Executors.newFixedThreadPool(4);

        this.fromCreator = queueServices.getCreationQueue();
        this.fromValidator = queueServices.getValidationQueue();
    }

    public void launch() {
        executorService.execute(new Creator(fromCreator));
        executorService.execute(new Validator(financeService, fromCreator, fromValidator));
        executorService.execute(new Worker(fromValidator));
        executorService.execute(new Reporter());
    }
}
