package com.shoppingmall.pet.controller;

import com.shoppingmall.pet.dto.PetResponseDTO;
import com.shoppingmall.pet.service.PetService;
import com.shoppingmall.user.dto.ApiResponse;
import com.shoppingmall.pet.dto.PetRequestDTO;
import com.sun.security.auth.UserPrincipal;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PetApiController {

  private final PetService petService;

  public PetApiController(PetService petService) {
    this.petService = petService;
  }

  @PostMapping("/pets")
  public ResponseEntity<ApiResponse<?>> addPet(@RequestBody PetRequestDTO petRequestDTO , Authentication authentication) {
    String userId = authentication.getName();
    petService.create(petRequestDTO , userId );

    return ResponseEntity.ok(ApiResponse.success());
  }

  @DeleteMapping("/pets")
  public ResponseEntity<ApiResponse<?>> deletePet(@RequestBody Map<String , Long> request , Authentication authentication) {
    System.out.println("요청왓다 딜리트");
    String userId = authentication.getName();
      long petId = request.get("id");
      petService.delete(petId , userId );
      return ResponseEntity.ok(ApiResponse.success());
  }


  @GetMapping("/pets/list")
  public ResponseEntity<ApiResponse<?>> getAllPets(Authentication authentication) {
    String userId = authentication.getName();
    List<PetResponseDTO> pets = petService.getAllPets(userId);
    return ResponseEntity.ok(ApiResponse.success(pets));
  }

}
