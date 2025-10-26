package com.fashion.hub.Controller;

import com.fashion.hub.Dto.UserOrderRow;
import com.fashion.hub.Dto.UserOrderStats;
import com.fashion.hub.Model.Order;
import com.fashion.hub.Model.Role;
import com.fashion.hub.Model.User;
import com.fashion.hub.Repository.OrderRepository;
import com.fashion.hub.Repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminController {
	
	
	private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public AdminController(UserRepository userRepository, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    // ✅ Admin Dashboard
    @GetMapping("/dashboard")
    public String showAdminDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null || !Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "admin/dashboard";
    }



    @GetMapping("/users")
    public String showUsers(HttpSession session, Model model) {
        User admin = (User) session.getAttribute("loggedUser");
        if (admin == null || !Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/login";
        }

        model.addAttribute("user", admin);

        // Get all customers
        List<User> customers = userRepository.findByRole(Role.CUSTOMER);

        // Precompute order stats for each customer
        List<UserOrderRow> rows = customers.stream().map(c -> {
            long pending = 0, processing = 0, shipped = 0, delivered = 0, cancelled = 0;

            List<Order> orders = orderRepository.findByUser(c);
            if (orders != null) {
                for (Order o : orders) {
                    if (o.getStatus() == null) continue;
                    switch (o.getStatus()) {
                        case PENDING -> pending++;
                        case PROCESSING -> processing++;
                        case SHIPPED -> shipped++;
                        case DELIVERED -> delivered++;
                        case CANCELLED -> cancelled++;
                    }
                }
            }

            return new UserOrderRow(
                    c.getId(),
                    c.getName() != null ? c.getName() : "-",
                    c.getEmail() != null ? c.getEmail() : "-",
                    c.getPhone() != null ? c.getPhone() : "-",
                    pending,
                    processing,
                    shipped,
                    delivered,
                    cancelled
            );
        }).toList();

        model.addAttribute("rows", rows);

        return "admin/users";
    }




    @GetMapping("/orders")
    public String showOrders(HttpSession session, Model model,
                             @RequestParam(value="status", required=false) Order.Status status) {
        User admin = (User) session.getAttribute("loggedUser");
        if (admin == null || !Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/login";
        }
        model.addAttribute("user", admin);

        List<Order> orders;
        if (status != null) {
            orders = orderRepository.findByStatus(status);
            model.addAttribute("selectedStatus", status.name());
        } else {
            orders = orderRepository.findAll();
            model.addAttribute("selectedStatus", "ALL");
        }

        model.addAttribute("orders", orders);
        model.addAttribute("statuses", Order.Status.values());

        // Status to Tailwind color mapping
        Map<Order.Status, String> statusColors = Map.of(
            Order.Status.PENDING, "text-yellow-600 font-semibold",
            Order.Status.PROCESSING, "text-blue-600 font-semibold",
            Order.Status.SHIPPED, "text-indigo-600 font-semibold",
            Order.Status.DELIVERED, "text-green-600 font-semibold",
            Order.Status.CANCELLED, "text-red-600 font-semibold"
        );
        model.addAttribute("statusColors", statusColors);

        return "admin/orders";
    }

    @PostMapping("/orders/update")
    public String updateOrderStatus(HttpSession session,
                                    @RequestParam Long orderId,
                                    @RequestParam Order.Status status) {
        User admin = (User) session.getAttribute("loggedUser");
        if (admin == null || !Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/login";
        }

        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            orderRepository.save(order);
        });

        return "redirect:/admin/orders";
    }

    
    
    
    

    // ✅ Reports
    @GetMapping("/reports")
    public String showReports(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null || !Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "admin/reports";
    }
}
