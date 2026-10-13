package com.hotelbooking.transport.client;

record HotelApiResponse<T>(
        int responseCode,
        String responseMessage,
        T data
) {
}
