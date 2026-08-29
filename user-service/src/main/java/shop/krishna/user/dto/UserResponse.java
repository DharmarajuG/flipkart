package shop.krishna.user.dto;

import shop.krishna.user.domain.User;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        String phone,
        String role
) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getPhone(), u.getRole().name());
    }
}
