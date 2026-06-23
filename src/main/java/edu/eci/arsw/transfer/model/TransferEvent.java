package edu.eci.arsw.transfer.model;

public record TransferEvent(
        String eventId,
        String transferId,
        String from,
        String to,
        Integer amount,
        String currency,
        String createdAt
) {
}
