package my.javacraft.soap2rest.rest.app.service;

import java.util.List;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.soap2rest.rest.app.dao.entity.Message;
import my.javacraft.soap2rest.rest.app.dao.MessageDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MessageService {

    @Autowired
    private MessageDao messageDao;

    private Random random;

    Random getRandom() {
        if (random == null) {
            random = new Random();
        }
        return random;
    }

    public Message getRandomMessage() {
        List<Message> messageList = messageDao.findAll();
        int messageId = getRandom().nextInt(messageList.size()) + 1;

        log.info("MessageId = " + messageId);
        return messageDao.findById((long) messageId).orElse(null);
    }


}
