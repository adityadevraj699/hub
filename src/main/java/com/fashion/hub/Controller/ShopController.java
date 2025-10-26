package com.fashion.hub.Controller;

import com.fashion.hub.Model.*;
import com.fashion.hub.Repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ShopController {

    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private WishlistItemRepository wishlistRepository;
    @Autowired private CartItemRepository cartRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;

    // ------------------ 🛍️ ALL PRODUCTS PAGE ------------------
    @GetMapping("/products")
    public String showAllProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            HttpSession session,
            Model model) {

        User currentUser = (User) session.getAttribute("loggedUser");
        List<Product> products = productRepository.findAll()
                .stream()
                .filter(p -> p.getStatus() == Product.Status.ACTIVE)
                .collect(Collectors.toList());

        if (keyword != null && !keyword.isEmpty()) {
            products = products.stream()
                    .filter(p -> p.getName().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (categoryId != null) {
            products = products.stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId))
                    .collect(Collectors.toList());
        }
        if (minPrice != null || maxPrice != null) {
            double min = minPrice != null ? minPrice : 0;
            double max = maxPrice != null ? maxPrice : Double.MAX_VALUE;
            products = products.stream()
                    .filter(p -> p.getPrice() >= min && p.getPrice() <= max)
                    .collect(Collectors.toList());
        }

        if (currentUser != null) {
            List<Long> wishlistIds = wishlistRepository.findByUser(currentUser)
                    .stream().map(w -> w.getProduct().getId()).toList();
            products.forEach(p -> p.setIsInWishlist(wishlistIds.contains(p.getId())));
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("title", "All Products");
        return "products";
    }

    // ------------------ 🧾 PRODUCT DETAIL ------------------
    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id, HttpSession session, Model model) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) return "redirect:/products";

        User currentUser = (User) session.getAttribute("loggedUser");
        if (currentUser != null) {
            boolean inWishlist = wishlistRepository.findByUser(currentUser)
                    .stream()
                    .anyMatch(w -> w.getProduct().getId().equals(id));
            product.setIsInWishlist(inWishlist);
        }

        model.addAttribute("product", product);
        model.addAttribute("currentUser", currentUser);
        return "product-detail";
    }

    // ------------------ ❤️ TOGGLE WISHLIST ------------------
    @GetMapping("/wishlist/toggle/{productId}")
    public String toggleWishlist(@PathVariable Long productId, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedUser");
        if (currentUser == null) return "redirect:/login";

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return "redirect:/products";

        WishlistItem existing = wishlistRepository.findByUserAndProduct(currentUser, product);
        if (existing == null) {
            WishlistItem newItem = new WishlistItem();
            newItem.setUser(currentUser);
            newItem.setProduct(product);
            wishlistRepository.save(newItem);
        } else {
            wishlistRepository.delete(existing);
        }

        // ✅ Correct redirect to the product detail page
        return "redirect:/products/" + productId;
    }

    // ------------------ ❤️ WISHLIST PAGE ------------------
    @GetMapping("/wishlist")
    public String showWishlist(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("loggedUser");
        if (currentUser == null) return "redirect:/login";

        List<WishlistItem> wishlist = wishlistRepository.findByUser(currentUser);
        model.addAttribute("wishlistItems", wishlist);
        return "wishlist";
    }

 // inside ShopController - update redirects and small fixes
    @PostMapping("/cart/add/{productId}")
    public String addToCart(@PathVariable Long productId, @RequestParam(defaultValue = "1") int quantity, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedUser");
        if (currentUser == null) return "redirect:/login";

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return "redirect:/products";

        CartItem existing = cartRepository.findByUserAndProduct(currentUser, product);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            cartRepository.save(existing);
        } else {
            CartItem item = new CartItem();
            item.setUser(currentUser);
            item.setProduct(product);
            item.setQuantity(quantity);
            cartRepository.save(item);
        }
        return "redirect:/cart"; // show the cart so user can checkout
    }

    
    @PostMapping("/cart/remove/{id}")
    public String removeFromCart(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedUser");
        if (currentUser == null) return "redirect:/login";

        CartItem item = cartRepository.findById(id).orElse(null);
        if (item != null && item.getUser().getId().equals(currentUser.getId())) {
            cartRepository.delete(item);
        }

        return "redirect:/cart";
    }


    // ------------------ 🛒 CART PAGE ------------------
    @GetMapping("/cart")
    public String showCart(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("loggedUser");
        if (currentUser == null) return "redirect:/login";

        List<CartItem> cartItems = cartRepository.findByUser(currentUser);
        double total = cartItems.stream()
                .mapToDouble(ci -> ci.getProduct().getPrice() * ci.getQuantity()).sum();

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        return "cart";
    }

}
