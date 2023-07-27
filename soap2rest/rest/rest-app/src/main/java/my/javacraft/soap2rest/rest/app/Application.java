package my.javacraft.soap2rest.rest.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(
        "my.javacraft.soap2rest.rest.app.dao"
)
@EntityScan(
        "my.javacraft.soap2rest.rest.app.dao.entity"
)
@SpringBootApplication(scanBasePackages = {
        "my.javacraft.soap2rest.utils",
        "my.javacraft.soap2rest.rest.app",
})
public class Application {

    public static void main(String[] args){
        SpringApplication.run(Application.class, args);
    }
}
