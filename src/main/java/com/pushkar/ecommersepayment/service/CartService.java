package com.pushkar.ecommersepayment.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pushkar.ecommersepayment.dto.AddToCartRequest;
import com.pushkar.ecommersepayment.dto.CartItemResponse;
import com.pushkar.ecommersepayment.model.CartItem;
import com.pushkar.ecommersepayment.model.Product;
import com.pushkar.ecommersepayment.repository.CartRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

  private final CartRepository cartRepository;
  private final ProductService productService;

  @Transactional
  public CartItem addToCart(AddToCartRequest request) {
    log.info("Adding to cart - User: {}, Product: {}, Quantity: {}",
        request.getUserId(), request.getProductId(), request.getQuantity());

    // Validate product exists and has stock
    Product product = productService.getProductById(request.getProductId());
    if (product.getStock() < request.getQuantity()) {
      throw new RuntimeException("Insufficient stock. Available: " + product.getStock());
    }

    // Check if item already exists in cart
    Optional<CartItem> existingItem = cartRepository.findByUserIdAndProductId(
        request.getUserId(), request.getProductId());

    if (existingItem.isPresent()) {
      CartItem item = existingItem.get();
      int newQuantity = item.getQuantity() + request.getQuantity();

      if (product.getStock() < newQuantity) {
        throw new RuntimeException("Insufficient stock. Available: " + product.getStock());
      }

      item.setQuantity(newQuantity);
      return cartRepository.save(item);
    } else {
      CartItem newItem = new CartItem();
      newItem.setUserId(request.getUserId());
      newItem.setProductId(request.getProductId());
      newItem.setQuantity(request.getQuantity());
      return cartRepository.save(newItem);
    }
  }

  public List<CartItemResponse> getCartItems(String userId) {
    log.info("Fetching cart items for user: {}", userId);
    List<CartItem> cartItems = cartRepository.findByUserId(userId);
    List<CartItemResponse> responses = new ArrayList<>();

    for (CartItem item : cartItems) {
      CartItemResponse response = new CartItemResponse();
      response.setId(item.getId());
      response.setUserId(item.getUserId());
      response.setProductId(item.getProductId());
      response.setQuantity(item.getQuantity());

      try {
        Product product = productService.getProductById(item.getProductId());
        response.setProduct(product);
      } catch (RuntimeException e) {
        log.warn("Product not found for cart item: {}", item.getProductId());
      }

      responses.add(response);
    }

    return responses;
  }

  @Transactional
  public void clearCart(String userId) {
    log.info("Clearing cart for user: {}", userId);
    cartRepository.deleteByUserId(userId);
  }

  public void removeCartItem(String cartItemId) {
    log.info("Removing cart item: {}", cartItemId);
    cartRepository.deleteById(cartItemId);
  }
}
