package com.example.pbanking.dto.response;

public record MakePaymentResponse (
    Data data,
    Object links,
    Object meta
) {
    public record Data (
        String paymentId,
        String status,
        String creationDateTime,
        String statusUpdateDateTime
    ) {}
}

// {
//     "data": {
//       "paymentId": "string",
//       "status": "string",
//       "creationDateTime": "string",
//       "statusUpdateDateTime": "string"
//     },
//     "links": {},
//     "meta": {}
// }
