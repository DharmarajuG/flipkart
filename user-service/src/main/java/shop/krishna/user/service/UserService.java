package shop.krishna.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.krishna.common.error.ResourceNotFoundException;
import shop.krishna.user.domain.Address;
import shop.krishna.user.domain.User;
import shop.krishna.user.dto.AddressRequest;
import shop.krishna.user.dto.AddressResponse;
import shop.krishna.user.dto.UserResponse;
import shop.krishna.user.repository.AddressRepository;
import shop.krishna.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        return UserResponse.from(loadUser(userId));
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> listAddresses(Long userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(AddressResponse::from)
                .toList();
    }

    @Transactional
    public AddressResponse addAddress(Long userId, AddressRequest req) {
        User user = loadUser(userId);
        Address address = Address.builder()
                .line1(req.line1())
                .line2(req.line2())
                .city(req.city())
                .state(req.state())
                .pincode(req.pincode())
                .country(req.country())
                .isDefault(req.isDefault())
                .build();
        user.addAddress(address);
        userRepository.save(user);
        return AddressResponse.from(address);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> ResourceNotFoundException.of("Address", addressId));
        if (!address.getUser().getId().equals(userId)) {
            throw ResourceNotFoundException.of("Address", addressId);
        }
        addressRepository.delete(address);
    }

    private User loadUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
    }
}
