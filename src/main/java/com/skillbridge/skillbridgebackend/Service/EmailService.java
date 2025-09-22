package com.skillbridge.skillbridgebackend.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:3000}")
    private String baseUrl;

    @Value("${app.name:SkillBridge}")
    private String appName;

    /**
     * Gửi email reset password
     */
    public void sendPasswordResetEmail(String toEmail, String fullName, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("[" + appName + "] Yêu cầu đặt lại mật khẩu");

            String resetUrl = baseUrl + "/auth/reset-password?token=" + resetToken;
            String htmlContent = buildPasswordResetEmailContent(fullName, resetUrl);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Không thể gửi email. Vui lòng thử lại sau.", e);
        }
    }

    /**
     * Gửi email xác nhận đổi mật khẩu thành công
     */
    public void sendPasswordChangeConfirmation(String toEmail, String fullName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[" + appName + "] Mật khẩu đã được thay đổi");

            String text = "Xin chào " + fullName + ",\n\n" +
                    "Mật khẩu của bạn đã được thay đổi thành công.\n\n" +
                    "Nếu bạn không thực hiện thao tác này, vui lòng liên hệ với chúng tôi ngay lập tức.\n\n" +
                    "Trân trọng,\n" +
                    appName + " Team";

            message.setText(text);
            mailSender.send(message);

            log.info("Password change confirmation email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send password change confirmation email to: {}", toEmail, e);
        }
    }

    /**
     * Tạo nội dung HTML cho email reset password
     */
    private String buildPasswordResetEmailContent(String fullName, String resetUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Đặt lại mật khẩu</title>
                <style>
                    .container { max-width: 600px; margin: 0 auto; font-family: Arial, sans-serif; }
                    .header { background-color: #4F46E5; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; background-color: #f8f9fa; }
                    .button { 
                        display: inline-block; 
                        padding: 12px 24px; 
                        background-color: #4F46E5; 
                        color: white; 
                        text-decoration: none; 
                        border-radius: 5px;
                        margin: 20px 0;
                    }
                    .footer { padding: 20px; text-align: center; color: #6b7280; font-size: 14px; }
                    .warning { color: #dc2626; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s</h1>
                    </div>
                    <div class="content">
                        <h2>Xin chào %s,</h2>
                        <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>
                        <p>Vui lòng nhấp vào nút bên dưới để tạo mật khẩu mới:</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Đặt lại mật khẩu</a>
                        </p>
                        <p>Hoặc copy và paste link sau vào trình duyệt:</p>
                        <p style="word-break: break-all; background-color: #e5e7eb; padding: 10px; border-radius: 3px;">
                            %s
                        </p>
                        <p class="warning">⚠️ Link này chỉ có hiệu lực trong 15 phút.</p>
                        <p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>
                    </div>
                    <div class="footer">
                        <p>Trân trọng,<br>%s Team</p>
                        <p>Email này được gửi tự động, vui lòng không trả lời.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(appName, fullName, resetUrl, resetUrl, appName);
    }

    /**
     * Test email configuration
     */
    public void testEmailConfiguration(String testEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(testEmail);
            message.setSubject("[" + appName + "] Test Email Configuration");
            message.setText("Email configuration test successful!");

            mailSender.send(message);
            log.info("Test email sent successfully to: {}", testEmail);

        } catch (Exception e) {
            log.error("Email configuration test failed", e);
            throw new RuntimeException("Email configuration test failed: " + e.getMessage());
        }
    }
}