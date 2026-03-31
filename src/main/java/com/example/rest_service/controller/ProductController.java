package com.example.rest_service.controller;

import com.example.rest_service.dto.ProductDTO;
import com.example.rest_service.entity.Product;
import com.example.rest_service.repository.ProductRepository;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController() {
        this.productRepository = new ProductRepository();
    }

    @GET
    public Response getAllProducts(@QueryParam("page") Integer page,
                                   @QueryParam("size") Integer size,
                                   @QueryParam("productName") String productName,
                                   @QueryParam("categoryName") String categoryName,
                                   @QueryParam("creationMin") String creationMin,
                                   @QueryParam("creationMax") String creationMax) {
        try {
            List<Product> products;

            if (page != null && size != null) {
                products = productRepository.findPaginated(page, size);
            } else if (productName != null || categoryName != null || creationMin != null || creationMax != null) {
                Instant min = creationMin != null ? Instant.parse(creationMin) : null;
                Instant max = creationMax != null ? Instant.parse(creationMax) : null;
                products = productRepository.findByCriteria(productName, categoryName, min, max);
            } else {
                products = productRepository.findAll();
            }

            List<ProductDTO> dtos = products.stream()
                    .map(p -> new ProductDTO(p.getId(), p.getName(), p.getPrice(), p.getCreationDateTime()))
                    .collect(Collectors.toList());
            return Response.ok(dtos).build();

        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new com.example.rest_service.dto.ErrorResponse(500, e.getMessage()))
                    .build();
        } catch (java.time.format.DateTimeParseException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new com.example.rest_service.dto.ErrorResponse(400,
                            "Invalid date format. Use ISO-8601 format."))
                    .build();
        }
    }
}