package com.pconk.procurement.api.resource;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.pconk.procurement.infrastructure.config.Logged;

import com.pconk.procurement.api.dto.SupplierDTO;
import com.pconk.procurement.api.dto.WebResponse;
import com.pconk.procurement.api.dto.PagedResponse;
import com.pconk.procurement.domain.service.SupplierService;

@Path("/suppliers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Logged // Semua method di class ini sekarang ter-log otomatis
public class SupplierResource {

    @Inject
    SupplierService supplierService;

    @GET
    public WebResponse<PagedResponse<SupplierDTO>> list(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        return WebResponse.success(supplierService.getAllSuppliers(page, size));
    }

    @GET
    @Path("/{id}")
    public WebResponse<SupplierDTO> get(@PathParam("id") Long id) {
        return WebResponse.success(supplierService.getSupplierById(id));
    }

    @POST    
    public WebResponse<SupplierDTO> create(@Valid SupplierDTO supplierDTO) {
        SupplierDTO created = supplierService.createSupplier(supplierDTO);
        return WebResponse.success(created);
    }

    @PUT
    @Path("/{id}")
    public WebResponse<SupplierDTO> update(@PathParam("id") Long id, @Valid SupplierDTO supplierDTO) {
        return WebResponse.success(supplierService.updateSupplier(id, supplierDTO));
    }

    @DELETE
    @Path("/{id}")
    public WebResponse<Void> delete(@PathParam("id") Long id) {
        supplierService.deleteSupplier(id);
        return WebResponse.success(null);
    }
}
