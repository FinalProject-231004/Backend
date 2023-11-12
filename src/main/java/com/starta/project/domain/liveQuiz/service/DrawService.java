package com.starta.project.domain.liveQuiz.service;

import com.starta.project.domain.liveQuiz.dto.DrawMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DrawService {

    private final List<DrawMessage> drawMessages = new ArrayList<>();

    public void saveDrawMessage(DrawMessage drawMessage) {
        drawMessages.add(drawMessage);
    }

    public List<DrawMessage> getAllDrawMessages() {
        return new ArrayList<>(drawMessages);
    }
}
