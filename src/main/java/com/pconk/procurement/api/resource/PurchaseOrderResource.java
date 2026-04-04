package com.pconk.procurement.api.resource;

import com.pconk.procurement.api.dto.PurchaseOrderRequestDTO;
import com.pconk.procurement.api.dto.PurchaseOrderStatusDTO;
import com.pconk.procurement.api.dto.PurchaseOrderResponseDTO;
import com.pconk.procurement.api.dto.WebResponse;
import com.pconk.procurement.domain.service.PurchaseOrderService;
import com.pconk.procurement.infrastructure.config.Logged;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/purchase-orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Logged
public class PurchaseOrderResource {

    @Inject
    PurchaseOrderService poService;

    @POST
    public WebResponse<PurchaseOrderResponseDTO> create(@Valid PurchaseOrderRequestDTO request) {
        return WebResponse.success(poService.createPO(request));
    }

    @GET
    @Path("/{id}")
    public WebResponse<PurchaseOrderResponseDTO> get(@PathParam("id") Long id) {
        return WebResponse.success(poService.getById(id));
    }
    
    @PUT
    @Path("/update-status/{id}")
    public WebResponse<PurchaseOrderResponseDTO> updateStatus(@PathParam("id") Long id, @Valid PurchaseOrderStatusDTO status) {
        return WebResponse.success(poService.updateStatus(id, status));
    }
}
