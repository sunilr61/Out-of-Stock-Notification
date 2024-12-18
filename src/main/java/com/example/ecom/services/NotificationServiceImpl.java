package com.example.ecom.services;

import com.example.ecom.exceptions.*;
import com.example.ecom.models.*;
import com.example.ecom.repositories.InventoryRepository;
import com.example.ecom.repositories.NotificationRepository;
import com.example.ecom.repositories.ProductRepository;
import com.example.ecom.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements  NotificationService{
    private UserRepository userRepository;
    private ProductRepository productRepository;
    private InventoryRepository inventoryRepository;
    private NotificationRepository notificationRepository;

    @Autowired
    public NotificationServiceImpl(UserRepository userRepository,
                                   ProductRepository productRepository,
                                   InventoryRepository inventoryRepository,
                                   NotificationRepository notificationRepository){
        this.userRepository=userRepository;
        this.productRepository=productRepository;
        this.inventoryRepository=inventoryRepository;
        this.notificationRepository=notificationRepository;
    }

    @Override
    public Notification registerUser(int userId, int productId) throws UserNotFoundException, ProductNotFoundException, ProductInStockException {
        Optional<User> userOptional = userRepository.findById(userId);
        if(userOptional.isEmpty()){
            throw new UserNotFoundException("User not found");
        }
        User user = userOptional.get();

        Optional<Product> productOptional = productRepository.findById(productId);
        if(productOptional.isEmpty()){
            throw new ProductNotFoundException("Product not found");
        }
        Product product = productOptional.get();

        Notification notification = new Notification();

        Optional<Inventory> inventoryOptional= inventoryRepository.findByProduct(product);
        if(inventoryOptional.isEmpty()){
            throw new ProductNotFoundException("Product not found in inventory");
        }
        Inventory inventory=inventoryOptional.get();
        if(inventory.getQuantity() != 0){
            throw new ProductInStockException("Product is in stock");
        }
        notification.setUser(user);
        notification.setProduct(product);
        notification.setStatus(NotificationStatus.PENDING);
        return notificationRepository.save(notification);

    }

    @Override
    public void deregisterUser(int userId, int notificationId) throws UserNotFoundException, NotificationNotFoundException, UnAuthorizedException {
        Optional<User> userOptional = userRepository.findById(userId);
        if(userOptional.isEmpty()){
            throw new UserNotFoundException("User not found");
        }
        Optional<Notification> notificationOptional = notificationRepository.findById(notificationId);
        if(notificationOptional.isEmpty()){
            throw new NotificationNotFoundException("Notification not found");
        }
        if(notificationOptional.get().getUser().getId() != userOptional.get().getId()){
            throw new UnAuthorizedException("Notification does not belong to the user");
        }
        notificationRepository.delete(notificationOptional.get());
    }
}
