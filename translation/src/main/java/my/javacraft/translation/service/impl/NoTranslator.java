package my.javacraft.translation.service.impl;

import my.javacraft.translation.service.TranslateService;
import org.springframework.stereotype.Service;

@Service
public class NoTranslator implements TranslateService {

    public String translate2English(String word) {
        return word;
    }
}
