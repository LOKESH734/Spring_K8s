package org.retro1.repository;

import org.retro1.model.Laptop;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Repository
public class Savelaptop {
    public void save2(Laptop laptop) {
        System.out.println("Saved sexcessfully");
    }
}
