package fr.railway.model;

import static org.assertj.core.api.Assertions.assertThat;

import fr.railway.shared.ExpectedFailedReason;
import fr.railway.shared.Reason;
import fr.railway.shared.Result;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class ResultTest {
    final Map<Boolean, String> actualResults = new HashMap<>();

    @Test
    void should_display_valid_message_on_success_toString() {
        final var actual = Result.success(2);

        assertThat(actual).hasToString("Success{2}");
    }

    @Test
    void should_display_valid_message_on_failure_toString() {
        final var actual = Result.failure(new ExpectedFailedReason("message"));

        assertThat(actual).hasToString("Failure{message}");
    }

    @Test
    void should_check_equality_on_success() {
        final var given = Result.success(2);

        assertThat(given).isEqualTo(Result.success(2));
    }

    @Test
    void should_check_equality_on_failure() {
        final var givenFailure = new ExpectedFailedReason("message");

        final var actual = Result.failure(givenFailure);

        assertThat(actual).isEqualTo(Result.failure(givenFailure));
    }

    @Test
    void should_apply_different_treatment_according_to_success_or_fail() {
        final var given = Stream.of(2, 3)
            .map(this::isDivisibleByTwo)
            .toList();

        given.forEach(this::applyTreatment);

        assertThat(actualResults).containsAllEntriesOf(Map.of(
            true, "Le digit suivant 2 est multiple de 2!",
            false, "Opération a échoué car 3 est non divisible par 2"
        ));
    }

    private Result<Integer, Reason> isDivisibleByTwo(int number) {
        return number % 2 == 0 ? Result.success(number) :
            Result.failure(
                new ExpectedFailedReason(String.format("%d est non divisible par 2", number)));
    }

    private void applyTreatment(Result<Integer, Reason> result) {
        result.either(digitInSuccess -> actualResults.put(true,
                String.format("Le digit suivant %d est multiple de 2!", digitInSuccess)),
            messageFailure -> actualResults.put(false,
                String.format("Opération a échoué car %s", messageFailure.failedReason())));
    }

    @Test
    void should_only_map_success_data_when_call_onSuccess() {
        final var given = Stream.of(4, 3)
            .map(this::isDivisibleByTwo)
            .toList();

        given.forEach(digit -> digit.then(Result.onSuccess(digitInSuccess -> digitInSuccess + 1))
            .either(
                digitInSuccess -> actualResults.put(true,
                    String.format("Le digit suivant %d a été incrementé!", digitInSuccess)),
                messageFailure -> actualResults.put(false,
                    String.format("Opération a échoué car %s", messageFailure.failedReason()))
            )
        );

        assertThat(actualResults).containsAllEntriesOf(Map.of(
            true, "Le digit suivant 5 a été incrementé!",
            false, "Opération a échoué car 3 est non divisible par 2"
        ));
    }

    @Test
    void should_only_map_failure_data_when_call_onFailure() {
        final var given = Stream.of(2, 3)
            .map(this::isDivisibleByTwo)
            .toList();

        given.forEach(digit -> digit.then(
                Result.mapFailure(digitFailed ->
                    new ExpectedFailedReason("Raison du rejet : " + digitFailed.failedReason())))
            .either(
                digitInSuccess -> actualResults.put(true,
                    String.format("Le digit suivant %d est inchangé!", digitInSuccess)),
                messageFailure -> actualResults.put(false, messageFailure.failedReason())
            )
        );

        assertThat(actualResults).containsAllEntriesOf(Map.of(
            true, "Le digit suivant 2 est inchangé!",
            false, "Raison du rejet : 3 est non divisible par 2"
        ));
    }

    @Test
    void when_attempt_can_produce_success_or_failure() {
        final var given = Stream.of(2, 3)
            .map(this::isDivisibleByTwo)
            .toList();

        given.forEach(digit -> digit.then(Result.map(this::isDivisibleByTwo))
            .either(
                digitInSuccess -> actualResults.put(true,
                    String.format("Le digit suivant %d est multiple de 2!", digitInSuccess)),
                messageFailure -> actualResults.put(false,
                    messageFailure.failedReason())
            )
        );

        assertThat(actualResults).containsAllEntriesOf(Map.of(
            true, "Le digit suivant 2 est multiple de 2!",
            false, "3 est non divisible par 2"
        ));
    }


    @Test
    void should_apply_separate_action_on_success_and_on_failure() {
        final var actualFailedEmails = new ArrayList<String>();

        final var validEmails  = Stream.of(
                Result.success("valid_email@"), Result.success("invalid_email"))
            .flatMap(email -> email
                .then(Result.map(this::isValidEmail))
                .then(Result.onSuccess(this::formatToUpperCase))
                .then(Result.mapFailure(message -> {
                    actualFailedEmails.add("Raison du rejet : " + message.failedReason());
                    return message;
                }))
                .then(Result.getSuccess())
            )
            .toList();

        assertThat(validEmails).containsExactly("VALID_EMAIL@");
        assertThat(actualFailedEmails)
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly("Raison du rejet : invalid_email n'est pas un email");
    }

    @Test
    void should_apply_attempt_only_on_previous_success_element() {
        final var actualFailedEmails = new ArrayList<String>();

        final var validEmails  = Stream.<Result<String, Reason>>of(
                Result.success("valid_email@"),
                Result.failure(new ExpectedFailedReason("invalid_email")))
            .flatMap(email -> email
                .then(Result.map(this::isValidEmail))
                .then(Result.onSuccess(this::formatToUpperCase))
                .then(Result.mapFailure(message -> {
                    actualFailedEmails.add("Raison du rejet : " + message.failedReason());
                    return message;
                }))
                .then(Result.getSuccess())
            )
            .toList();

        assertThat(validEmails).containsExactly("VALID_EMAIL@");
        assertThat(actualFailedEmails).containsExactly(
            "Raison du rejet : invalid_email");
    }

    @Test
    void should_chain_multiple_action() {
        final var actualFailedEmails = new ArrayList<String>();

        final var validEmails  = Stream.of(
                Result.success("valid_email@email.fr"),
                Result.success("invalid_email"))
            .flatMap(email -> email
                .then(Result.map(this::isValidEmail))
                .then(Result.map(this::isValidDomainEmail))
                .then(Result.peekFailureAndDo(message -> actualFailedEmails.add(
                    "Raison du rejet : " + message.failedReason())))
            )
            .toList();

        assertThat(validEmails).containsExactly("valid_email@email.fr");
        assertThat(actualFailedEmails).containsExactly(
            "Raison du rejet : invalid_email n'est pas un email");
    }

    private Result<String, Reason> isValidEmail(String email) {
        return email.contains("@") ? Result.success(email) :
            Result.failure(new ExpectedFailedReason(String.format("%s n'est pas un email", email)));
    }

    private Result<String, Reason> isValidDomainEmail(String email) {
        return email.contains(".") ? Result.success(email) :
            Result.failure(
                new ExpectedFailedReason(String.format("%s n'est pas un domain", email)));
    }

    private String formatToUpperCase(String email) {
        return email.toUpperCase();
    }
}