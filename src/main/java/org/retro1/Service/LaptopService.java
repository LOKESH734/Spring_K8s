package org.retro1.Service;

import org.retro1.model.Laptop;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LaptopService {
    public boolean isGoodLap(Laptop laptop) {
        return true;
    }
}
