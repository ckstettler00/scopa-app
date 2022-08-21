package com.stettler.scopa.scopaserver.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stettler.scopa.scopaserver.converter.GameEventToScopaMessageString;
import com.stettler.scopa.scopaserver.converter.StringToGameEvent;
import com.stettler.scopa.scopaserver.converter.StringToScopaMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class ConverterConfig  implements WebMvcConfigurer {

    @Bean
    public ObjectMapper mapper() {
        return new ObjectMapper();
    }
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToGameEvent());
        registry.addConverter(new GameEventToScopaMessageString());
        registry.addConverter(new StringToScopaMessage());
    }
}
