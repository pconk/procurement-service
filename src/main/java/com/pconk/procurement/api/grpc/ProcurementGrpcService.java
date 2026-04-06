package com.pconk.procurement.api.grpc;

import com.pconk.procurement.v1.ProcurementService;
import com.pconk.procurement.v1.UpdateStatusRequest;
import com.pconk.procurement.v1.UpdateStatusResponse;
import com.pconk.procurement.domain.service.PurchaseOrderService;
import com.pconk.procurement.domain.entity.PurchaseOrderItem;
import com.pconk.procurement.infrastructure.grpc.WarehouseClient;
import com.pconk.procurement.api.dto.PurchaseOrderStatusDTO;
import com.pconk.procurement.domain.entity.OrderStatus;
import io.quarkus.grpc.GrpcService;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import java.util.List;
import java.time.Duration;
import io.grpc.Status;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

@GrpcService
public class ProcurementGrpcService implements ProcurementService {
    private static final Logger LOG = Logger.getLogger(ProcurementGrpcService.class);

    @Inject
    PurchaseOrderService poService;

    @Inject
    WarehouseClient warehouseClient;

    @Blocking // Solusi untuk Error JTA: Menjalankan di Worker Thread
    @Override
    public Uni<UpdateStatusResponse> updateStatusToReceived(UpdateStatusRequest request) {
        LOG.infof("Step 0: Updating status [Thread: %s]", Thread.currentThread().getName());
        final Long poId = request.getPoId();
        return Uni.createFrom().item(() -> {
            LOG.infof("Step 1: Updating status to RECEIVED [Thread: %s]", Thread.currentThread().getName());
            PurchaseOrderStatusDTO dto = new PurchaseOrderStatusDTO();
            dto.status = OrderStatus.RECEIVED;
            poService.updateStatus(poId, dto);
            return poId;
        })
                .chain(id -> {
                    LOG.infof("Step 2: Sending stock update to warehouse-api [Thread: %s]",
                            Thread.currentThread().getName());
                    List<PurchaseOrderItem> items = poService.getItems(id);
                    return warehouseClient.increaseStock(items)
                            .ifNoItem().after(Duration.ofSeconds(10)).fail();
                })
                .onItem().invoke(success -> {
                    if (Boolean.TRUE.equals(success)) {
                        LOG.infof("Step 3: Updating stock timestamp [Thread: %s]",
                                Thread.currentThread().getName());
                        
                        poService.markStockAsUpdated(poId);
                        
                        LOG.info("Step 4: All steps completed, returning response.");
                    }
                })
                .replaceWith(UpdateStatusResponse.newBuilder().setSuccess(true).build())
                // Cara mudah & cepat: Map exception manual ke gRPC Status
                .onFailure().transform(ex -> {
                    if (ex instanceof NotFoundException) {
                        return Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException();
                    } else if (ex instanceof RuntimeException) {
                        return Status.FAILED_PRECONDITION.withDescription(ex.getMessage()).asRuntimeException();
                    }
                    return Status.INTERNAL.withDescription(ex.getMessage()).asRuntimeException();
                });
    }
}