package shop.krishna.user.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import shop.krishna.user.dto.AddressRequest;
import shop.krishna.user.dto.AddressResponse;
import shop.krishna.user.dto.UserResponse;
import shop.krishna.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Profile and address management (authenticated)")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Current user's profile")
    public UserResponse me(Authentication auth) {
        return userService.getProfile(currentUserId(auth));
    }

    @GetMapping("/me/addresses")
    public List<AddressResponse> addresses(Authentication auth) {
        return userService.listAddresses(currentUserId(auth));
    }

    @PostMapping("/me/addresses")
    public ResponseEntity<AddressResponse> addAddress(Authentication auth,
                                                      @Valid @RequestBody AddressRequest req) {
        AddressResponse created = userService.addAddress(currentUserId(auth), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/me/addresses/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAddress(Authentication auth, @PathVariable Long addressId) {
        userService.deleteAddress(currentUserId(auth), addressId);
    }

    /** The gateway sets the authenticated principal to the numeric user id. */
    private Long currentUserId(Authentication auth) {
        return Long.valueOf(auth.getName());
    }
}
