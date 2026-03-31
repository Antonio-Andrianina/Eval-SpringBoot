package com.example.rest_service.controller;

import com.example.rest_service.dto.DishDTO;
import com.example.rest_service.dto.IngredientDTO;
import com.example.rest_service.entity.Dish;
import com.example.rest_service.entity.DishIngredient;
import com.example.rest_service.repository.DishRepository;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Path("/dishes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DishController {

    private final DishRepository dishRepository;

    public DishController() {
        this.dishRepository = new DishRepository();
    }

    @GET
    public Response getAllDishes() {
        try {
            List<Dish> dishes = dishRepository.findAll();
            List<DishDTO> dtos = dishes.stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
            return Response.ok(dtos).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new com.example.dto.ErrorResponse(500, e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}/ingredients")
    public Response updateDishIngredients(@PathParam("id") Integer id, List<Integer> ingredientIds) {
        try {
            if (ingredientIds == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new com.example.dto.ErrorResponse(400,
                                "Request body containing list of ingredient IDs is required."))
                        .build();
            }

            Dish updatedDish = dishRepository.updateIngredients(id, ingredientIds);
            DishDTO dto = mapToDTO(updatedDish);
            return Response.ok(dto).build();

        } catch (com.example.exception.DishNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new com.example.dto.ErrorResponse(404, e.getMessage()))
                    .build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new com.example.dto.ErrorResponse(500, e.getMessage()))
                    .build();
        }
    }

    private DishDTO mapToDTO(Dish dish) {
        List<IngredientDTO> ingredientDTOs = new ArrayList<>();
        if (dish.getDishIngredients() != null) {
            ingredientDTOs = dish.getDishIngredients().stream()
                    .map(DishIngredient::getIngredient)
                    .map(i -> new IngredientDTO(i.getId(), i.getName(), i.getPrice(), i.getCategory()))
                    .collect(Collectors.toList());
        }
        return new DishDTO(dish.getId(), dish.getName(), dish.getSellingPrice(), ingredientDTOs);
    }
}