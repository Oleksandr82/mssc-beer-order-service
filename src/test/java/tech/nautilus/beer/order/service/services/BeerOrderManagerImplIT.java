package tech.nautilus.beer.order.service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import tech.nautilus.beer.order.service.domain.BeerOrder;
import tech.nautilus.beer.order.service.domain.BeerOrderLine;
import tech.nautilus.beer.order.service.domain.BeerOrderStatusEnum;
import tech.nautilus.beer.order.service.domain.Customer;
import tech.nautilus.beer.order.service.repositories.BeerOrderRepository;
import tech.nautilus.beer.order.service.repositories.CustomerRepository;
import tech.nautilus.beer.order.service.services.beer.BeerServiceImpl;
import tech.nautilus.brewery.model.BeerDto;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static tech.nautilus.beer.order.service.services.testcomponents.BeerOrderValidationListener.FAIL_VALIDATION;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock
public class BeerOrderManagerImplIT {

    @Autowired
    BeerOrderManager beerOrderManager;

    @Autowired
    BeerOrderRepository beerOrderRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    ObjectMapper objectMapper;

    // Mock TastingRoomService to suppress scheduled tasks
    @MockBean
    TastingRoomService tastingRoomService;

    Customer testCustomer;

    UUID beerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        testCustomer = customerRepository.save(Customer.builder()
                .customerName("Test Customer")
                .build());
    }

    @Test
    void testTransitionNewToAllocated() throws JsonProcessingException {

        // Set up stubs
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();
        stubFor(get(BeerServiceImpl.BEER_UPC_PATH_V1 + beerDto.getUpc())
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        // Perform tests
        BeerOrder newBeerOrder = createBeerOrder(beerDto);
        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(newBeerOrder);

        await().atMost(5, SECONDS).until(() -> beerOrderRepository.findById(newBeerOrder.getId())
                .map(order -> BeerOrderStatusEnum.ALLOCATED == order.getOrderStatus())
                .orElse(false));

        BeerOrder savedBeerOrder2 = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        // Analyse results
        assertNotNull(savedBeerOrder);
        assertAll(
                () -> assertEquals(BeerOrderStatusEnum.ALLOCATED, savedBeerOrder2.getOrderStatus()),
                () -> assertTrue(savedBeerOrder2.getBeerOrderLines().stream()
                        .map(line -> line.getOrderQuantity() == line.getQuantityAllocated())
                        .allMatch(allItemsAllocated -> allItemsAllocated))
        );
    }

    @Test
    void testFailedValidation() throws JsonProcessingException {

        // Set up stubs
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();
        stubFor(get(BeerServiceImpl.BEER_UPC_PATH_V1 + beerDto.getUpc())
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        // Perform tests
        BeerOrder newBeerOrder = createBeerOrder(beerDto);
        newBeerOrder.setCustomerRef(FAIL_VALIDATION);

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(newBeerOrder);

        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findById(newBeerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.VALIDATION_EXCEPTION, foundOrder.getOrderStatus());
        });
    }

    @Test
    void testTransitionNewToPickedUp() throws JsonProcessingException {

        // Set up stubs
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();
        stubFor(get(BeerServiceImpl.BEER_UPC_PATH_V1 + beerDto.getUpc())
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        // Perform tests
        BeerOrder newBeerOrder = createBeerOrder(beerDto);
        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(newBeerOrder);

        await().atMost(5, SECONDS).until(() -> beerOrderRepository.findById(newBeerOrder.getId())
                .map(order -> BeerOrderStatusEnum.ALLOCATED == order.getOrderStatus())
                .orElse(false));

        beerOrderManager.beerOrderPickedUp(savedBeerOrder.getId());

        await().atMost(5, SECONDS).until(() -> beerOrderRepository.findById(newBeerOrder.getId())
                .map(order -> BeerOrderStatusEnum.PICKED_UP == order.getOrderStatus())
                .orElse(false));

        BeerOrder savedBeerOrderPickedUp = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        // Analyse results
        assertNotNull(savedBeerOrderPickedUp);
        assertEquals(BeerOrderStatusEnum.PICKED_UP, savedBeerOrderPickedUp.getOrderStatus());
    }

    public BeerOrder createBeerOrder(BeerDto beerDto) {

        BeerOrder beerOrder = BeerOrder.builder()
                .customer(testCustomer)
                .build();

        Set<BeerOrderLine> lines = new HashSet<>();
        lines.add(BeerOrderLine.builder()
                .beerId(beerDto.getId())
                .upc(beerDto.getUpc())
                .orderQuantity(1)
                .beerOrder(beerOrder)
                .build());
        beerOrder.setBeerOrderLines(lines);

        return beerOrder;
    }
}
