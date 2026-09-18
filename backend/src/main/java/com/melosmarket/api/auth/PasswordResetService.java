package com.melosmarket.api.auth;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;

import com.melosmarket.api.auth.persistence.PasswordResetTokenEntity;
import com.melosmarket.api.auth.persistence.PasswordResetTokenRepository;
import com.melosmarket.api.auth.persistence.UserEntity;
import com.melosmarket.api.auth.persistence.UserRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PasswordResetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final JavaMailSender mailSender;
    private final String publicBaseUrl;
    private final String fromEmail;
    private final int validHours;

    public PasswordResetService(
            PasswordResetTokenRepository tokenRepository,
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            JavaMailSender mailSender,
            @Value("${melosmarket.public-base-url}") String publicBaseUrl,
            @Value("${melosmarket.email.from}") String fromEmail,
            @Value("${melosmarket.email.password-reset-valid-hours}") int validHours) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.mailSender = mailSender;
        this.publicBaseUrl = publicBaseUrl;
        this.fromEmail = fromEmail;
        this.validHours = validHours;
    }

    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByEmailIgnoreCase(email.trim().toLowerCase())
                .ifPresent(this::sendPasswordResetEmail);
    }

    @Transactional
    public void resetPassword(String tokenValue, String password) {
        PasswordResetTokenEntity token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid password reset token"));
        if (token.getUsedAt() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password reset token already used");
        }
        if (token.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password reset token expired");
        }

        UserEntity user = token.getUser();
        user.setPasswordHash(passwordHasher.hash(password));
        token.setUsedAt(OffsetDateTime.now());
    }

    private void sendPasswordResetEmail(UserEntity user) {
        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setUser(user);
        token.setToken(createToken());
        token.setExpiresAt(OffsetDateTime.now().plusHours(validHours));
        tokenRepository.save(token);

        try {
            sendEmail(user.getEmail(), passwordResetLink(token.getToken()));
        } catch (MessagingException | MailException exception) {
            LOGGER.error("Could not send password reset message to {}", user.getEmail(), exception);
        }
    }

    private void sendEmail(String toEmail, String resetLink) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        helper.setTo(toEmail);
        helper.setFrom(fromEmail);
        helper.setSubject("Jelszó visszaállítása - Melos Market");
        helper.setText(emailHtml(resetLink), true);
        mailSender.send(message);
    }

    private String passwordResetLink(String token) {
        return publicBaseUrl.replaceAll("/+$", "") + "/?passwordResetToken=" + token;
    }

    private String createToken() {
        byte[] bytes = new byte[48];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String emailHtml(String resetLink) {
        return """
                <!doctype html>
                <html lang="hu">
                  <body style="margin:0;background:#f4f6f8;font-family:Arial,sans-serif;color:#111827;">
                    <div style="max-width:620px;margin:0 auto;padding:32px 18px;">
                      <div style="background:#ffffff;border-radius:18px;padding:32px;border:1px solid #e5e7eb;">
                        <div style="font-size:14px;font-weight:700;color:#16a34a;margin-bottom:14px;">Melos Market</div>
                        <h1 style="font-size:26px;line-height:1.2;margin:0 0 14px;">Jelszó visszaállítása</h1>
                        <p style="font-size:16px;line-height:1.6;margin:0 0 20px;color:#374151;">
                          Kérés érkezett a Melos Market fiókod jelszavának visszaállítására. Az alábbi gombbal új jelszót adhatsz meg.
                        </p>
                        <a href="%s" style="display:inline-block;background:#111827;color:#ffffff;text-decoration:none;border-radius:999px;padding:14px 22px;font-weight:700;">
                          Új jelszó megadása
                        </a>
                        <p style="font-size:13px;line-height:1.5;color:#6b7280;margin:22px 0 0;">
                          A link %d óráig érvényes. Ha nem te kérted a jelszó visszaállítását, ezt az emailt nyugodtan figyelmen kívül hagyhatod.
                        </p>
                      </div>
                    </div>
                  </body>
                </html>
                """.formatted(resetLink, validHours);
    }
}
