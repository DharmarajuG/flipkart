package shop.krishna.user.dto;

import shop.krishna.user.domain.Address;

public record AddressResponse(
        Long id,
        String line1,
        String line2,
        String city,
        String state,
        String pincode,
        String country,
        boolean isDefault
) {
    public static AddressResponse from(Address a) {
        return new AddressResponse(a.getId(), a.getLine1(), a.getLine2(), a.getCity(),
                a.getState(), a.getPincode(), a.getCountry(), a.isDefault());
    }
}
