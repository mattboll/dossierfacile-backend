package fr.dossierfacile.scheduler.tasks.tenantwarning;

import com.google.common.base.Strings;
import fr.dossierfacile.common.entity.ConfirmationToken;
import fr.dossierfacile.common.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sendinblue.ApiException;
import sibApi.TransactionalEmailsApi;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailTo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarningMailSender {

    private final TransactionalEmailsApi apiInstance;
    @Value("${sendinblue.url.domain}")
    private String sendinBlueUrlDomain;
    @Value("${sendinblue.template.id.first.warning.for.deletion.of.documents}")
    private Long templateFirstWarningForDeletionOfDocuments;
    @Value("${sendinblue.template.id.second.warning.for.deletion.of.documents}")
    private Long templateSecondWarningForDeletionOfDocuments;

    public void sendEmailFirstWarningForDeletionOfDocuments(User user, ConfirmationToken confirmationToken) {
        if (isNotBlank(user.getEmail())) {
            log.info("Sending FIRST warning to tenant {}", user.getId());
            sendWarningMail(user, confirmationToken, templateFirstWarningForDeletionOfDocuments);
        }
    }

    public void sendEmailSecondWarningForDeletionOfDocuments(User user, ConfirmationToken confirmationToken) {
        if (isNotBlank(user.getEmail())) {
            log.info("Sending SECOND warning to tenant {}", user.getId());
            sendWarningMail(user, confirmationToken, templateSecondWarningForDeletionOfDocuments);
        }
    }

    private void sendWarningMail(User user, ConfirmationToken confirmationToken, Long templateId) {
        Map<String, String> variables = new HashMap<>();
        variables.put("PRENOM", user.getFirstName());
        variables.put("NOM", Strings.isNullOrEmpty(user.getPreferredName()) ? user.getLastName() : user.getPreferredName());
        variables.put("confirmToken", confirmationToken.getToken());
        variables.put("sendinBlueUrlDomain", sendinBlueUrlDomain);

        SendSmtpEmailTo sendSmtpEmailTo = new SendSmtpEmailTo();
        sendSmtpEmailTo.setEmail(user.getEmail());
        if (!Strings.isNullOrEmpty(user.getFullName())) {
            sendSmtpEmailTo.setName(user.getFullName());
        }

        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
        sendSmtpEmail.templateId(templateId);
        sendSmtpEmail.params(variables);
        sendSmtpEmail.to(Collections.singletonList(sendSmtpEmailTo));

        try {
            apiInstance.sendTransacEmail(sendSmtpEmail);
        } catch (ApiException e) {
            log.error("Email api exception", e);
        }
    }
}
