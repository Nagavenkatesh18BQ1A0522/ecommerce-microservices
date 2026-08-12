package com.shop.orders.order;

import com.shop.orders.inventory.InventoryClient;
import com.shop.orders.inventory.InventoryView;
import com.shop.orders.order.dto.CreateOrderRequest;
import com.shop.orders.order.dto.OrderResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    InventoryClient inventoryClient;

    @InjectMocks
    OrderService orderService;

    @Test
    void createOrder_buildsOrderFromRequest_andSavesIt() {
        // Arrange
        when(inventoryClient.getStock("BOOK-123")).thenReturn(new InventoryView("BOOK-123", 50));
        CreateOrderRequest request =
                new CreateOrderRequest(1L, "BOOK-123", 2, new BigDecimal("49.99"));
        // when save() is called, just return whatever Order was passed in
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        OrderResponse response = orderService.createOrder(request);

        // Assert 1: capture the Order that was passed to save(), check it was built correctly
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order saved = captor.getValue();

        assertThat(saved.getCustomerId()).isEqualTo(1L);
        assertThat(saved.getProductCode()).isEqualTo("BOOK-123");
        assertThat(saved.getQuantity()).isEqualTo(2);
        assertThat(saved.getAmount()).isEqualByComparingTo("49.99");
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.PLACED);   // service set this
        assertThat(saved.getCreatedAt()).isNotNull();                   // service set this

        // Assert 2: the response is mapped from the saved order
        assertThat(response.productCode()).isEqualTo("BOOK-123");
        assertThat(response.status()).isEqualTo(OrderStatus.PLACED);
    }

    @Test
    void getOrder_throwsOrderNotFound_whenMissing(){

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.getOrder(999L))
                .isInstanceOf(OrderNotFound.class);

    }
}