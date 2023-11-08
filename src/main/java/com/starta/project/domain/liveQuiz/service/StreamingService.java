package com.starta.project.domain.liveQuiz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class StreamingService {


    @Autowired
    private ResourceLoader resourceLoader;

    public Mono<Resource> getVideo(String title){
        Resource resource = resourceLoader.getResource("classpath:static/videos/" + title + ".mp4");
        return Mono.just(resource);
    }
}
