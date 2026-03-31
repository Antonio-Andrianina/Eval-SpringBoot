package com.example.rest_service.controller;

import com.example.rest_service.dto.IngredientDTO;
import com.example.rest_service.dto.StockValueDTO;
import com.example.rest_service.entity.CategoryEnum;
import com.example.rest_service.entity.Ingredient;
import com.example.rest_service.repository.IngredientRepository;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Path("/ingredients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class IngredientController {

    private final IngredientRepository ingredientRepository;

    public IngredientController() {
        this.ingredientRepository = new IngredientRepository();
    }

    @GET
    public Response getAllIngredients() {
        try {
            List<Ingredient> ingredients = ingredientRepository.findAll();
            List<IngredientDTO> dtos = ingredients.stream()
                    .map(i -> new IngredientDTO(i.getId(), i.getName(), i.getPrice(), i.getCategory()))
                    .collect(Collectors.toList());
            return Response.ok(dtos).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new com.example.rest_service.dto.ErrorResponse(500, e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getIngredientById(@PathParam("id") Integer id) {
        try {
            Ingredient ingredient = ingredientRepository.findById(id);
            IngredientDTO dto = new IngredientDTO(ingredient.getId(), ingredient.getName(),
                    ingredient.getPrice(), ingredient.getCategory());
            return Response.ok(dto).build();
        } catch (com.example.exception.IngredientNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new com.example.dto.ErrorResponse(404, e.getMessage()))
                    .build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new com.example.dto.ErrorResponse(500, e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{id}/stock")
    public Response getStockValue(@PathParam("id") Integer id,
                                  @QueryParam("at") String at,
                                  @QueryParam("unit") String unit) {
        try {
            if (at == null || at.isEmpty() || unit == null || unit.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new com.example.dto.ErrorResponse(400,
                                "Either mandatory query parameter `at` or `unit` is not provided."))
                        .build();
            }

            // Check if ingredient exists
            ingredientRepository.findById(id);

            Instant instant = Instant.parse(at);
            Double stockValue = ingredientRepository.getStockValueAt(id, instant, unit);

            StockValueDTO dto = new StockValueDTO(unit, stockValue);
            return Response.ok(dto).build();

        } catch (com.example.exception.IngredientNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new com.example.dto.ErrorResponse(404, e.getMessage()))
                    .build();
        } catch (java.time.format.DateTimeParseException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new com.example.dto.ErrorResponse(400,
                            "Invalid date format for parameter 'at'. Use ISO-8601 format."))
                    .build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new com.example.dto.ErrorResponse(500, e.getMessage()))
                    .build();
        }
    }
}
