package org.retro1.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Alien {

    @Value("69")
    private int age;

    private Computer comp;

    public Alien() {
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Computer getComp() {
        return comp;
    }

    @Autowired
    @Qualifier("laptop") // Change to "desktop" if you want Desktop instead
    public void setComp(Computer comp) {
        this.comp = comp;
    }

    public Alien(int age, Computer comp) {
        this.age = age;
        this.comp = comp;
    }
}
