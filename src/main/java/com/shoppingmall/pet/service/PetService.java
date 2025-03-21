package com.shoppingmall.pet.service;

import com.shoppingmall.pet.dto.PetRequestDTO;
import com.shoppingmall.pet.dto.PetResponseDTO;
import com.shoppingmall.pet.model.Pet;
import com.shoppingmall.pet.repository.PetRepository;
import com.shoppingmall.user.model.User;
import com.shoppingmall.user.repository.UserRepository;
import com.shoppingmall.user.service.UserService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PetService {

  private final PetRepository petRepository;
  private final UserService userService;
  private final UserRepository userRepository;

  public PetService(PetRepository petRepository, UserService userService,
      UserRepository userRepository) {
    this.petRepository = petRepository;
    this.userService = userService;
    this.userRepository = userRepository;
  }

  public void create(PetRequestDTO petRequestDTO , String userId) {
    User user = userRepository.findByUserId(userId);
    petRepository.save(petRequestDTO.toEntity(user));
  }

  public List<PetResponseDTO> getAllPets(String userId) {
    User user = userRepository.findByUserId(userId);

    return petRepository.findByUser(user).stream()
                                              .map(PetResponseDTO :: from)
                                              .toList();
  }

  public void delete(long petId, String userId) {
    User user = userRepository.findByUserId(userId);
    Pet pet = petRepository.findById(petId).orElse(null);

    if(pet != null) {
      if(user.getId().equals(pet.getUser().getId())) {
        petRepository.delete(pet);
      }
    }

  }
}
