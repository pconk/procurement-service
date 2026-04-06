package com.pconk.procurement.infrastructure.grpc;

import com.pconk.warehouse.v1.WarehouseService;
import com.pconk.warehouse.v1.StockUpdateRequest;
import com.pconk.procurement.api.grpc.ProcurementGrpcService;
import com.pconk.warehouse.v1.StockItem;
import com.pconk.warehouse.v1.StockUpdateResponse;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

@ApplicationScoped
public class WarehouseClient {

        private static final Logger LOG = Logger.getLogger(ProcurementGrpcService.class);

    @GrpcClient("warehouse")
    WarehouseService warehouseService;

    public Uni<Boolean> increaseStock(List<com.pconk.procurement.domain.entity.PurchaseOrderItem> items) {
        // Konversi Entity Java ke Protobuf message
        List<StockItem> protoItems = items.stream()
                .map(item -> StockItem.newBuilder()
                        .setItemId(item.itemId)
                        .setQuantity(item.quantity)
                        .build())
                .collect(Collectors.toList());

        StockUpdateRequest request = StockUpdateRequest.newBuilder()
                .addAllItems(protoItems)
                .build();
        LOG.info("StockUpdateRequest");        
        LOG.info(request);

        // Memanggil service Go secara asinkron/reactive (Uni)
        // warehouseService.increaseStock(request)
        //         .subscribe().with(
        //             response -> LOG.info("Stock successfully updated in Go service"),
        //             failure -> LOG.errorf("Failed to update stock in Go: %s", failure.getMessage())
        //         );

        return Uni.createFrom().item(() -> {
                return true;
        });
        // return warehouseService.increaseStock(request)
        //         .map(StockUpdateResponse::getSuccess);
    }
}