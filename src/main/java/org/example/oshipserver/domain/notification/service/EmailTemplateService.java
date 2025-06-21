package org.example.oshipserver.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    private final TemplateEngine templateEngine;

    public String renderEmail(String title, String contentHtml) {
        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("content", contentHtml);
        return templateEngine.process("email/email", context);
    }
}
