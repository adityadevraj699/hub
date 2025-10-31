package com.fashion.hub.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fashion.hub.Model.Category;
import com.fashion.hub.Repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private Cloudinary cloudinary;

    public List<Category> getAllCategories(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return categoryRepository.findByNameContainingIgnoreCase(keyword);
        }
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    /**
     * Save or update category safely
     */
    public Category saveCategory(Category category, MultipartFile imageFile) throws IOException {
        if (category.getId() != null) {
            // Update case: fetch existing category
            Category existingCategory = categoryRepository.findById(category.getId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));

            existingCategory.setName(category.getName());
            existingCategory.setDescription(category.getDescription());

            // Image update
            if (imageFile != null && !imageFile.isEmpty()) {
                Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(),
                        ObjectUtils.asMap("folder", "fashionhub/categories"));
                existingCategory.setImage((String) uploadResult.get("secure_url"));
            }

            // Products preserve automatically
            return categoryRepository.save(existingCategory);

        } else {
            // New save case
            if (imageFile != null && !imageFile.isEmpty()) {
                Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(),
                        ObjectUtils.asMap("folder", "fashionhub/categories"));
                category.setImage((String) uploadResult.get("secure_url"));
            }
            return categoryRepository.save(category);
        }
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
