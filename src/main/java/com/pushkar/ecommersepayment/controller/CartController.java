package com.pushkar.ecommersepayment.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pushkar.ecommersepayment.dto.AddToCartRequest;
import com.pushkar.ecommersepayment.dto.CartItemResponse;
import com.pushkar.ecommersepayment.model.CartItem;
import com.pushkar.ecommersepayment.service.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

  private final CartService cartService;

  @PostMapping("/add")
  public ResponseEntity<CartItem> addToCart(@Valid @RequestBody AddToCartRequest request) {
    CartItem cartItem = cartService.addToCart(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(cartItem);
  }

  @GetMapping("/{userId}")
  public ResponseEntity<List<CartItemResponse>> getCartItems(@PathVariable String userId) {
    List<CartItemResponse> cartItems = cartService.getCartItems(userId);
    return ResponseEntity.ok(cartItems);
  }

  @DeleteMapping("/{userId}/clear")
  public ResponseEntity<Map<String, String>> clearCart(@PathVariable String userId) {
    cartService.clearCart(userId);
    return ResponseEntity.ok(Map.of("message", "Cart cleared successfully"));
  }

  @DeleteMapping("/item/{cartItemId}")
  public ResponseEntity<Map<String, String>> removeCartItem(@PathVariable String cartItemId) {
    cartService.removeCartItem(cartItemId);
    return ResponseEntity.ok(Map.of("message", "Item removed from cart"));
  }
}
