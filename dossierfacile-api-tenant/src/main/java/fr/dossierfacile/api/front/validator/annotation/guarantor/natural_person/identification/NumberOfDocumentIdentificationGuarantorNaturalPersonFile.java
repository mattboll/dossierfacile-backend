package fr.dossierfacile.api.front.validator.annotation.guarantor.natural_person.identification;


import fr.dossierfacile.api.front.validator.guarantor.natural_person.identification.NumberOfDocumentIdentificationGuarantorNaturalPersonFileValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)

@Constraint(
        validatedBy = {NumberOfDocumentIdentificationGuarantorNaturalPersonFileValidator.class}
)

public @interface NumberOfDocumentIdentificationGuarantorNaturalPersonFile {
    String message() default "number of document must be between 1 and 5";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}