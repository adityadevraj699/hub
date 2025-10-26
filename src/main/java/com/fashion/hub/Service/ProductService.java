package com.fashion.hub.Service;

import com.fashion.hub.Model.Product;
import com.fashion.hub.Model.Category;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    List<Product> getAllProducts(String keyword);
    Product getProductById(Long id);
    Product saveProduct(Product product) throws Exception; // only product
    String uploadImage(MultipartFile file) throws Exception; // separate upload
    void deleteProduct(Long id);
    List<Product> getProductsByCategory(Category category);
}
