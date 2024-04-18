# railway-programming
An implementation of the Railway Oriented Programming pattern in Java

classic : using only the Result class
shared : more complex implementation with the use of ErrorFormat sharded among all failed

# Example
classic :
       Stream.<Result<String, String>>of(
                Result.success("valid_email@email.fr"),
                Result.success("invalid_email"))
            .flatMap(email -> email
                .then(Result.map(this::isValidEmail))
                .then(Result.onFailureDo(message -> actualFailedEmails.add("Raison du rejet : " + message)))
                .then(Result.map(this::isValidDomainEmail))
                .then(Result.success())
            )
            .toList();

shared :
        Stream.of(
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

    private Result<String, Reason> isValidEmail(String email) {
        return email.contains("@") ? Result.success(email) :
            Result.failure(new ExpectedFailedReason(String.format("%s n'est pas un email", email)));
    }

    private Result<String, Reason> isValidDomainEmail(String email) {
        return email.contains(".") ? Result.success(email) :
            Result.failure(
                new ExpectedFailedReason(String.format("%s n'est pas un domain", email)));
    }
