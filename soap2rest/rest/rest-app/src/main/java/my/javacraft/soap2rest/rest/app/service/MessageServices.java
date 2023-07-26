package my.javacraft.soap2rest.rest.app.service;

import java.util.Random;
import my.javacraft.soap2rest.rest.app.dao.Message;
import my.javacraft.soap2rest.rest.app.dao.MessageDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by nikilipa on 3/28/17.
 */
//@Schema(name = "s2r_rest_message", resource = "/liquibase/MessageChangelog.xml")
@Service
public class MessageServices {

    private final static Logger log = LoggerFactory.getLogger(MessageServices.class);

    @Autowired
    private MessageDao messageDao;

    private int totalNumber = 0;
    private final Random random = new Random();

    public void updateTotalNumber() {
        this.totalNumber = messageDao.findAll().size() - 1;
    }

    public Message getRandomMessage() {
        if (totalNumber <= 0) {
            updateTotalNumber();
        }
        int messageId = random.nextInt(totalNumber) + 1;

        log.info("MessageId = " + messageId);
        return messageDao.findById((long) messageId);
    }


}
