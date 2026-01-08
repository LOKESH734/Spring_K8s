package org.retro1;

import org.retro1.Service.LaptopService;
import org.retro1.model.Alien;
import org.retro1.model.Computer;
import org.retro1.model.Laptop;
import org.retro1.repository.Savelaptop;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class Retro1Application {

    public static void main(String[] args) {

        ApplicationContext context=SpringApplication.run(Retro1Application.class, args);
       LaptopService service=context.getBean(LaptopService.class);
       Savelaptop save=context.getBean(Savelaptop.class);
        Laptop lap=context.getBean(Laptop.class);
        save.save2(lap);
    }

}
