package my.javacraft.soap2rest.rest.app.rest;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.soap2rest.rest.app.dao.MessageDao;
import my.javacraft.soap2rest.rest.app.dao.entity.Message;
import my.javacraft.soap2rest.rest.app.service.MessageService;
import my.javacraft.soap2rest.utils.interceptor.ExecutionTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "/sync")
public class SyncResource {

    @Autowired
    private MessageDao messageDao;

    @Autowired
    private MessageService messageService;

    @ExecutionTime
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAbout() {
        return "Message resource V1 which provided sync interface.";
    }

    @ExecutionTime
    @GetMapping(value = "/message",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Message>> getAllMessages() {
        return ResponseEntity
                .ok(messageDao.findAll());
    }

    @ExecutionTime
    @GetMapping(value = "/message/random",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Message> getRandomMessage() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(messageService.getRandomMessage());
    }

    @ExecutionTime
    @GetMapping(value = "/message/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Message> getMessageById(@PathVariable Long id) {
        return ResponseEntity
                .ok(messageDao
                        .findById(id)
                        .orElseThrow(() -> new RuntimeException("Message not found."))
                );
    }

    @ExecutionTime
    @PutMapping(value = "/message",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Message> putMessage(@RequestBody String text) {
        Message message = new Message();
        message.setMessage(text);
        message = messageDao.save(message);
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(message);
    }

}
