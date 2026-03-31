package com.example.rest_service.controller;

import com.example.rest_service.dto.CreateDishDTO;
import com.example.rest_service.dto.DishDTO;
import com.example.rest_service.dto.ErrorResponse;
import com.example.rest_service.dto.IngredientDTO;
import com.example.rest_service.entity.Dish;
import com.example.rest_service.entity.DishIngredient;
import com.example.rest_service.exception.DishAlreadyExistsException;
import com.example.rest_service.repository.DishRepository;
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
    public Response getAllDishes(@QueryParam("priceUnder") Double priceUnder,
                                 @QueryParam("priceOver") Double priceOver,
                                 @QueryParam("name") String name) {
        try {
            List<Dish> dishes;

            if (priceUnder != null || priceOver != null || (name != null && !name.trim().isEmpty())) {
                dishes = dishRepository.findAllWithFilters(priceUnder, priceOver, name);
            } else {
                dishes = dishRepository.findAll();
            }

            List<DishDTO> dtos = dishes.stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
            return Response.ok(dtos).build();

        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, e.getMessage()))
                    .build();
        }
    }


    @POST
    public Response createDishes(List<CreateDishDTO> dishDTOs) {
        try {
            if (dishDTOs == null || dishDTOs.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse(400, "Request body must contain at least one dish to create."))
                        .build();
            }

            for (int i = 0; i < dishDTOs.size(); i++) {
                for (int j = i + 1; j < dishDTOs.size(); j++) {
                    if (dishDTOs.get(i).getName().equalsIgnoreCase(dishDTOs.get(j).getName())) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity(new ErrorResponse(400,
                                        "Duplicate dish names found in request: " + dishDTOs.get(i).getName()))
                                .build();
                    }
                }
            }

            for (CreateDishDTO dto : dishDTOs) {
                if (dishRepository.existsByName(dto.getName())) {
                    throw new DishAlreadyExistsException(dto.getName());
                }
            }

            List<Dish> createdDishes = dishRepository.createDishes(dishDTOs);

            List<DishDTO> dtos = createdDishes.stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());

            return Response.status(Response.Status.CREATED)
                    .entity(dtos)
                    .build();

        } catch (DishAlreadyExistsException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, e.getMessage()))
                    .build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}/ingredients")
    public Response updateDishIngredients(@PathParam("id") Integer id, List<Integer> ingredientIds) {
        try {
            if (ingredientIds == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse(400,
                                "Request body containing list of ingredient IDs is required."))
                        .build();
            }

            Dish updatedDish = dishRepository.updateIngredients(id, ingredientIds);
            DishDTO dto = mapToDTO(updatedDish);
            return Response.ok(dto).build();

        } catch (com.example.rest_service.exception.DishNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(404, e.getMessage()))
                    .build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, e.getMessage()))
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