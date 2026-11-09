package com.melosmarket.api.admin;

import java.util.List;
import java.util.Map;

import com.melosmarket.api.auth.AuthContext;
import com.melosmarket.api.auth.persistence.AccountRole;
import com.melosmarket.api.auth.persistence.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;

@RestController
@RequestMapping("/admin/emails")
public class AdminEmailController {
    private final AuthContext auth;
    private final UserRepository users;
    private final JavaMailSender mail;
    private final String from;

    public AdminEmailController(AuthContext auth, UserRepository users, JavaMailSender mail,
            @Value("${melosmarket.email.from}") String from) {
        this.auth = auth;
        this.users = users;
        this.mail = mail;
        this.from = from;
    }

    public record EmailDraft(@NotBlank @Size(max = 180) String subject,
            @NotBlank @Size(max = 180) String heading,
            @NotBlank @Size(max = 10000) String message,
            @NotBlank @Pattern(regexp = "ALL|WORKER|CUSTOMER|SUPPLIER") String audience) {}

    private void requireAdmin() {
        if (auth.requireUser().role() != AccountRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/preview")
    public Map<String, Object> preview(@Valid @RequestBody EmailDraft draft) {
        requireAdmin();
        return Map.of("html", html(draft), "recipients", recipients(draft).size());
    }

    @PostMapping("/test")
    public Map<String, Integer> test(@Valid @RequestBody EmailDraft draft) {
        requireAdmin();
        send(auth.requireUser().email(), draft);
        return Map.of("sent", 1, "failed", 0);
    }

    @PostMapping("/send")
    public Map<String, Integer> send(@Valid @RequestBody EmailDraft draft) {
        requireAdmin();
        int sent = 0;
        int failed = 0;
        for (String recipient : recipients(draft)) {
            try {
                send(recipient, draft);
                sent++;
            } catch (ResponseStatusException exception) {
                failed++;
            }
        }
        return Map.of("sent", sent, "failed", failed);
    }

    private List<String> recipients(EmailDraft draft) {
        return users.findAll().stream()
                .filter(user -> user.isEmailVerified())
                .filter(user -> user.getRole() != AccountRole.ADMIN)
                .filter(user -> draft.audience().equals("ALL") || user.getRole().name().equals(draft.audience()))
                .map(user -> user.getEmail()).distinct().toList();
    }

    private void send(String recipient, EmailDraft draft) {
        try {
            var message = mail.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(recipient);
            helper.setSubject(draft.subject());
            helper.setText(draft.heading() + "\n\n" + draft.message(), html(draft));
            mail.send(message);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Nem sikerült elküldeni az emailt.");
        }
    }

    private String html(EmailDraft draft) {
        return """
                <!doctype html><html lang="hu"><body style="margin:0;background:#f4f6f8;font-family:Arial,sans-serif;color:#111827">
                <div style="max-width:620px;margin:0 auto;padding:32px 18px">
                <div style="background:white;border-radius:18px;padding:32px;border:1px solid #e5e7eb">
                <p style="color:#16a34a;font-weight:700">Melos Market</p>
                <h1 style="font-size:26px;line-height:1.3">%s</h1>
                <div style="font-size:16px;line-height:1.7;color:#374151;overflow-wrap:anywhere">%s</div>
                <p style="margin-top:28px;color:#6b7280;font-size:13px">Szolgáltatási tájékoztató a Melos Market fiókodhoz.</p>
                </div></div></body></html>
                """.formatted(HtmlUtils.htmlEscape(draft.heading()),
                        HtmlUtils.htmlEscape(draft.message()).replace("\n", "<br>"));
    }
}
