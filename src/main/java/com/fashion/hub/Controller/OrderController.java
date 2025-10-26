package com.fashion.hub.Controller;

import com.fashion.hub.Model.*;
import com.fashion.hub.Repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class OrderController {

    @Autowired private ProductRepository productRepository;
    @Autowired private CartItemRepository cartRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private WishlistItemRepository wishlistRepository; // if needed

    @Autowired
    private UserRepository userRepository;
    // Show checkout page for entire cart
    @GetMapping("/checkout")
    public String checkoutAll(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null) return "redirect:/login";

        List<CartItem> cartItems = cartRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        double total = cartItems.stream().mapToDouble(ci -> ci.getProduct().getPrice() * ci.getQuantity()).sum();
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("user", user);
        return "checkout";
    }

    // Show checkout page for single product (Buy Now)
    @GetMapping("/checkout/single/{productId}")
    public String checkoutSingle(@PathVariable Long productId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null) return "redirect:/login";

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return "redirect:/products";

        model.addAttribute("singleProduct", product);
        model.addAttribute("user", user);
        return "buy-now";
    }

    @PostMapping("/order/place")
public String placeOrder(@RequestParam Map<String, String> allParams,
                         @RequestParam(required = false) Long productId,
                         @RequestParam String address,
                         @RequestParam String paymentMethod,
                         HttpSession session, Model model) {

    User user = (User) session.getAttribute("loggedUser");
    if (user == null) return "redirect:/login";

    // Validate address
    if (address == null || address.trim().isEmpty()) {
        model.addAttribute("error", "Please provide shipping address.");
        if (productId != null) {
            model.addAttribute("singleProduct", productRepository.findById(productId).orElse(null));
            return "buy-now";
        } else {
            List<CartItem> cartItems = cartRepository.findByUser(user);
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("total", cartItems.stream()
                    .mapToDouble(ci -> ci.getProduct().getPrice() * ci.getQuantity())
                    .sum());
            return "checkout";
        }
    }

    // Update user's address
    user.setAddress(address);
    userRepository.save(user);
    session.setAttribute("loggedUser", user);

    Order order = new Order();
    order.setUser(user);
    order.setStatus(Order.Status.PENDING);
    order.setTotalPrice(0.0);
    orderRepository.save(order);

    List<OrderItem> orderItems = new ArrayList<>();
    double total = 0.0;

    // Single product "Buy Now"
    if (productId != null) {
        Integer quantity = Integer.parseInt(allParams.getOrDefault("quantity", "1"));
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return "redirect:/products";

        if (product.getQuantity() < quantity) {
            model.addAttribute("error", "Not enough stock available for this product.");
            model.addAttribute("singleProduct", product);
            return "buy-now";
        }

        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setPrice(product.getPrice());
        orderItems.add(item);

        total += product.getPrice() * quantity;
    } 
    // Cart checkout
    else {
        int index = 0;
        while (allParams.containsKey("productId_" + index)) {
            Long pid = Long.parseLong(allParams.get("productId_" + index));
            int qty = Integer.parseInt(allParams.get("quantity_" + index));

            Product product = productRepository.findById(pid).orElse(null);
            if (product == null) return "redirect:/cart";

            if (product.getQuantity() < qty) {
                model.addAttribute("error", "Not enough stock for product: " + product.getName());
                List<CartItem> cartItems = cartRepository.findByUser(user);
                model.addAttribute("cartItems", cartItems);
                model.addAttribute("total", cartItems.stream()
                        .mapToDouble(ci -> ci.getProduct().getPrice() * ci.getQuantity())
                        .sum());
                return "checkout";
            }

            product.setQuantity(product.getQuantity() - qty);
            productRepository.save(product);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(qty);
            item.setPrice(product.getPrice());
            orderItems.add(item);

            total += product.getPrice() * qty;
            index++;
        }
    }

    order.setTotalPrice(total);
    orderRepository.save(order);

    for (OrderItem oi : orderItems) {
        oi.setOrder(order);
        orderItemRepository.save(oi);
    }

    // Clear cart
    if (productId == null) {
        List<CartItem> cartItems = cartRepository.findByUser(user);
        cartRepository.deleteAll(cartItems);
    }

    return "redirect:/orders/confirmation/" + order.getId();
}


    // Order confirmation page
    @GetMapping("/orders/confirmation/{orderId}")
    public String orderConfirmation(@PathVariable Long orderId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null) return "redirect:/login";

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getUser().getId().equals(user.getId())) {
            return "redirect:/orders";
        }

        model.addAttribute("order", order);
        model.addAttribute("orderStatus", order.getStatus().name());

        return "order-confirmation";
    }

    // Order history page
    @GetMapping("/order/history")
    public String orderHistory(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null) return "redirect:/login";

        List<Order> orders = orderRepository.findByUser(user);
        model.addAttribute("orders", orders);
        return "order-history";
    }
    
    
 // View order history detail page
    @GetMapping("/order/history/{orderId}")
    public String viewOrderDetail(@PathVariable Long orderId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null) return "redirect:/login";

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getUser().getId().equals(user.getId())) {
            return "redirect:/order/history";
        }

        model.addAttribute("order", order);
        model.addAttribute("items", order.getItems());
        return "order-history-detail";
    }

}
